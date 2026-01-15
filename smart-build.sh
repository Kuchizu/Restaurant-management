#!/bin/bash

# Smart Build Script - собирает только изменённые сервисы
# Использование: ./smart-build.sh [--all] [--force service-name]

set -e

SERVICES=(
    "eureka-server"
    "config-server"
    "api-gateway"
    "order-service"
    "kitchen-service"
    "menu-service"
    "inventory-service"
    "billing-service"
)

# Инфраструктурные сервисы, которые нужно собирать первыми
INFRA_SERVICES=("eureka-server" "config-server")

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[OK]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Получить хеш последней сборки сервиса
get_last_build_hash() {
    local service=$1
    local hash_file=".build-cache/${service}.hash"
    if [ -f "$hash_file" ]; then
        cat "$hash_file"
    else
        echo ""
    fi
}

# Вычислить текущий хеш исходников сервиса
get_current_hash() {
    local service=$1
    # Хешируем все исходники сервиса + корневые build файлы
    find "$service/src" -type f 2>/dev/null | sort | xargs cat 2>/dev/null | md5sum | cut -d' ' -f1
}

# Сохранить хеш сборки
save_build_hash() {
    local service=$1
    local hash=$2
    mkdir -p .build-cache
    echo "$hash" > ".build-cache/${service}.hash"
}

# Проверить, нужна ли пересборка
needs_rebuild() {
    local service=$1
    local current_hash=$(get_current_hash "$service")
    local last_hash=$(get_last_build_hash "$service")

    if [ "$current_hash" != "$last_hash" ]; then
        return 0  # true - нужна пересборка
    fi
    return 1  # false - не нужна
}

# Получить список изменённых сервисов через git
get_git_changed_services() {
    local changed_services=()

    # Получаем изменённые файлы относительно последнего коммита
    local changed_files=$(git diff --name-only HEAD 2>/dev/null || git status --porcelain | awk '{print $2}')

    for service in "${SERVICES[@]}"; do
        if echo "$changed_files" | grep -q "^${service}/"; then
            changed_services+=("$service")
        fi
    done

    # Если изменились корневые файлы сборки - пересобрать всё
    if echo "$changed_files" | grep -qE "^(build\.gradle|settings\.gradle|gradle\.properties)$"; then
        log_warn "Изменились корневые файлы сборки - будут пересобраны все сервисы"
        echo "${SERVICES[@]}"
        return
    fi

    echo "${changed_services[@]}"
}

# Собрать один сервис
build_service() {
    local service=$1
    local current_hash=$(get_current_hash "$service")

    log_info "Сборка $service..."

    if docker-compose build "$service"; then
        save_build_hash "$service" "$current_hash"
        log_success "$service собран успешно"
        return 0
    else
        log_error "Ошибка сборки $service"
        return 1
    fi
}

# Основная логика
main() {
    local force_all=false
    local force_service=""
    local only_changed=true

    # Парсинг аргументов
    while [[ $# -gt 0 ]]; do
        case $1 in
            --all|-a)
                force_all=true
                shift
                ;;
            --force|-f)
                force_service="$2"
                shift 2
                ;;
            --help|-h)
                echo "Smart Build - интеллектуальная сборка изменённых сервисов"
                echo ""
                echo "Использование: $0 [опции]"
                echo ""
                echo "Опции:"
                echo "  --all, -a          Пересобрать все сервисы"
                echo "  --force, -f NAME   Принудительно собрать указанный сервис"
                echo "  --help, -h         Показать эту справку"
                exit 0
                ;;
            *)
                log_error "Неизвестный аргумент: $1"
                exit 1
                ;;
        esac
    done

    # Включаем BuildKit для ускорения
    export DOCKER_BUILDKIT=1
    export COMPOSE_DOCKER_CLI_BUILD=1

    local services_to_build=()

    if [ "$force_all" = true ]; then
        log_info "Принудительная пересборка всех сервисов..."
        services_to_build=("${SERVICES[@]}")
    elif [ -n "$force_service" ]; then
        log_info "Принудительная сборка: $force_service"
        services_to_build=("$force_service")
    else
        # Определяем изменённые сервисы
        log_info "Анализ изменений..."

        local changed=$(get_git_changed_services)

        if [ -z "$changed" ]; then
            # Если git не показал изменений, проверяем по хешам
            for service in "${SERVICES[@]}"; do
                if needs_rebuild "$service"; then
                    services_to_build+=("$service")
                fi
            done
        else
            IFS=' ' read -ra services_to_build <<< "$changed"
        fi
    fi

    if [ ${#services_to_build[@]} -eq 0 ]; then
        log_success "Все сервисы актуальны, сборка не требуется!"
        exit 0
    fi

    log_info "Сервисы для сборки: ${services_to_build[*]}"
    echo ""

    # Сначала собираем инфраструктурные сервисы
    for infra in "${INFRA_SERVICES[@]}"; do
        for service in "${services_to_build[@]}"; do
            if [ "$service" = "$infra" ]; then
                build_service "$service"
            fi
        done
    done

    # Затем остальные сервисы параллельно
    local pids=()
    for service in "${services_to_build[@]}"; do
        # Пропускаем инфраструктурные - они уже собраны
        local is_infra=false
        for infra in "${INFRA_SERVICES[@]}"; do
            if [ "$service" = "$infra" ]; then
                is_infra=true
                break
            fi
        done

        if [ "$is_infra" = false ]; then
            build_service "$service" &
            pids+=($!)
        fi
    done

    # Ждём завершения параллельных сборок
    local failed=0
    for pid in "${pids[@]}"; do
        if ! wait "$pid"; then
            failed=1
        fi
    done

    echo ""
    if [ $failed -eq 0 ]; then
        log_success "Сборка завершена успешно!"
    else
        log_error "Некоторые сервисы не удалось собрать"
        exit 1
    fi
}

main "$@"
