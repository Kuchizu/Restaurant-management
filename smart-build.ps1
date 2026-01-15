# Smart Build Script - собирает только изменённые сервисы
# Использование: .\smart-build.ps1 [-All] [-Force service-name]

param(
    [switch]$All,
    [string]$Force,
    [switch]$Help
)

$ErrorActionPreference = "Stop"

$SERVICES = @(
    "eureka-server"
    "config-server"
    "api-gateway"
    "order-service"
    "kitchen-service"
    "menu-service"
    "inventory-service"
    "billing-service"
)

$INFRA_SERVICES = @("eureka-server", "config-server")

function Write-Info($msg) { Write-Host "[INFO] $msg" -ForegroundColor Blue }
function Write-Success($msg) { Write-Host "[OK] $msg" -ForegroundColor Green }
function Write-Warn($msg) { Write-Host "[WARN] $msg" -ForegroundColor Yellow }
function Write-Err($msg) { Write-Host "[ERROR] $msg" -ForegroundColor Red }

function Get-LastBuildHash($service) {
    $hashFile = ".build-cache\$service.hash"
    if (Test-Path $hashFile) {
        return Get-Content $hashFile
    }
    return ""
}

function Get-CurrentHash($service) {
    $files = Get-ChildItem -Path "$service\src" -Recurse -File -ErrorAction SilentlyContinue
    if ($files) {
        $content = $files | ForEach-Object { Get-Content $_.FullName -Raw } | Out-String
        $md5 = [System.Security.Cryptography.MD5]::Create()
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($content)
        $hash = $md5.ComputeHash($bytes)
        return [BitConverter]::ToString($hash) -replace '-'
    }
    return ""
}

function Save-BuildHash($service, $hash) {
    New-Item -ItemType Directory -Path ".build-cache" -Force | Out-Null
    $hash | Out-File -FilePath ".build-cache\$service.hash" -NoNewline
}

function Test-NeedsRebuild($service) {
    $current = Get-CurrentHash $service
    $last = Get-LastBuildHash $service
    return $current -ne $last
}

function Get-GitChangedServices {
    $changedServices = @()

    try {
        $changedFiles = git diff --name-only HEAD 2>$null
        if (-not $changedFiles) {
            $changedFiles = git status --porcelain | ForEach-Object { $_.Substring(3) }
        }
    } catch {
        $changedFiles = @()
    }

    foreach ($service in $SERVICES) {
        if ($changedFiles | Where-Object { $_ -match "^$service/" }) {
            $changedServices += $service
        }
    }

    # Если изменились корневые файлы сборки
    if ($changedFiles | Where-Object { $_ -match "^(build\.gradle|settings\.gradle|gradle\.properties)$" }) {
        Write-Warn "Изменились корневые файлы сборки - будут пересобраны все сервисы"
        return $SERVICES
    }

    return $changedServices
}

function Build-Service($service) {
    $currentHash = Get-CurrentHash $service

    Write-Info "Сборка $service..."

    try {
        docker-compose build $service
        Save-BuildHash $service $currentHash
        Write-Success "$service собран успешно"
        return $true
    } catch {
        Write-Err "Ошибка сборки $service"
        return $false
    }
}

# Main
if ($Help) {
    Write-Host @"
Smart Build - интеллектуальная сборка изменённых сервисов

Использование: .\smart-build.ps1 [опции]

Опции:
  -All          Пересобрать все сервисы
  -Force NAME   Принудительно собрать указанный сервис
  -Help         Показать эту справку
"@
    exit 0
}

# Включаем BuildKit
$env:DOCKER_BUILDKIT = "1"
$env:COMPOSE_DOCKER_CLI_BUILD = "1"

$servicesToBuild = @()

if ($All) {
    Write-Info "Принудительная пересборка всех сервисов..."
    $servicesToBuild = $SERVICES
} elseif ($Force) {
    Write-Info "Принудительная сборка: $Force"
    $servicesToBuild = @($Force)
} else {
    Write-Info "Анализ изменений..."

    $changed = Get-GitChangedServices

    if ($changed.Count -eq 0) {
        foreach ($service in $SERVICES) {
            if (Test-NeedsRebuild $service) {
                $servicesToBuild += $service
            }
        }
    } else {
        $servicesToBuild = $changed
    }
}

if ($servicesToBuild.Count -eq 0) {
    Write-Success "Все сервисы актуальны, сборка не требуется!"
    exit 0
}

Write-Info "Сервисы для сборки: $($servicesToBuild -join ', ')"
Write-Host ""

$failed = $false

# Сначала инфраструктурные сервисы
foreach ($infra in $INFRA_SERVICES) {
    if ($servicesToBuild -contains $infra) {
        if (-not (Build-Service $infra)) {
            $failed = $true
        }
    }
}

# Затем остальные сервисы
foreach ($service in $servicesToBuild) {
    if ($INFRA_SERVICES -notcontains $service) {
        if (-not (Build-Service $service)) {
            $failed = $true
        }
    }
}

Write-Host ""
if (-not $failed) {
    Write-Success "Сборка завершена успешно!"
} else {
    Write-Err "Некоторые сервисы не удалось собрать"
    exit 1
}
