# Архитектура системы

## Транзакции

Транзакции используются в трех критичных местах:

**Создание заказа** (`OrderService.createOrder()`)
- Нужно, чтобы несколько официантов не могли одновременно создать заказ для одного столика
- Вся операция (создание заказа + резервирование столика) выполняется атомарно

**Резервирование ингредиентов** (`InventoryService.reserveIngredientsForOrder()`)
- Предотвращает ситуацию, когда два заказа одновременно резервируют последние ингредиенты
- Используется pessimistic lock для последовательного доступа

**Финализация заказа** (`BillingService.finalizeOrder()`)
- Применение скидки, расчет налогов и создание чека должны быть атомарными
- Иначе возможны ситуации с двойным списанием или частичным применением скидки

## Структура БД

**Many-to-Many:**
- Блюдо ↔ Ингредиент (через `dish_ingredients`)

**One-to-Many:**
- Категория → Блюда
- Столик → Заказы
- Официант → Заказы
- Заказ → Позиции заказа
- Поставщик → Заказы на поставку
- Ингредиент → Записи на складе

**Many-to-Many с полями:**
- Заказ на поставку ↔ Ингредиент (через `SupplyOrderIngredient` с полями `quantity` и `price_per_unit`)

## Пагинация

- Для списка заказов (`/api/orders`) - без общего количества (бесконечная прокрутка)
- Для таблиц (`/api/tables`, `/api/inventory`, `/api/ingredients`) - с общим количеством в заголовке `X-Total-Count`
- Максимальный размер страницы - 50 записей

## Enums

Все enum'ы хранятся как строки в БД (через `@Enumerated(EnumType.STRING)`):
- `EmployeeRole`: ADMIN, WAITER, CHEF, LOGISTICIAN
- `OrderStatus`: CREATED, IN_KITCHEN, PREPARING, READY, DELIVERED, CLOSED
- `DishStatus`: PENDING, IN_PROGRESS, READY, SERVED
- `SupplyOrderStatus`: CREATED, ORDERED, IN_TRANSIT, RECEIVED, CANCELLED
- `TableStatus`: FREE, OCCUPIED, RESERVED, CLEANING

Это удобно для отладки и чтения данных в БД.

## Entity и DTO

**Entity** - модели для работы с БД, содержат JPA аннотации.  
**DTO** - модели для API, содержат валидацию, без JPA аннотаций.

Маппинг между ними выполняется вручную в сервисах и контроллерах.
