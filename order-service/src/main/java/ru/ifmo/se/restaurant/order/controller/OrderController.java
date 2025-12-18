package ru.ifmo.se.restaurant.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.dto.EmployeeDto;
import ru.ifmo.se.restaurant.order.dto.ErrorResponse;
import ru.ifmo.se.restaurant.order.dto.OrderDto;
import ru.ifmo.se.restaurant.order.dto.OrderItemDto;
import ru.ifmo.se.restaurant.order.dto.TableDto;
import ru.ifmo.se.restaurant.order.service.OrderService;
import ru.ifmo.se.restaurant.order.util.PaginationUtil;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Service", description = "Управление заказами")
public class OrderController {
    private final OrderService orderService;

    @Operation(
        summary = "Создать новый заказ",
        description = "Создает новый заказ для указанного стола и официанта",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Минимальный payload для создания заказа",
            content = @Content(
                schema = @Schema(implementation = OrderDto.class),
                examples = {
                    @ExampleObject(
                        name = "Минимальный запрос",
                        summary = "Только обязательные поля",
                        value = """
                            {
                              "tableId": 5,
                              "waiterId": 2
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "С пожеланиями",
                        summary = "С необязательными полями",
                        value = """
                            {
                              "tableId": 5,
                              "waiterId": 2,
                              "specialRequests": "Без лука",
                              "items": [
                                {
                                  "dishId": 12,
                                  "quantity": 2,
                                  "specialRequest": "Без майонеза"
                                }
                              ]
                            }
                            """
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Заказ успешно создан",
            content = @Content(schema = @Schema(implementation = OrderDto.class))),
        @ApiResponse(responseCode = "400", description = "Некорректные данные запроса",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Стол или официант не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Стол уже занят",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderDto> createOrder(@Valid @RequestBody OrderDto dto) {
        return orderService.createOrder(dto);
    }

    @Operation(summary = "Получить заказ по ID", description = "Возвращает информацию о заказе по его идентификатору")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Заказ найден",
            content = @Content(schema = @Schema(implementation = OrderDto.class))),
        @ApiResponse(responseCode = "404", description = "Заказ не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public Mono<OrderDto> getOrderById(
            @Parameter(description = "ID заказа", required = true) @PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @Operation(summary = "Получить все заказы", description = "Возвращает список всех заказов без пагинации")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список заказов успешно получен")
    })
    @GetMapping
    public Flux<OrderDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    @Operation(summary = "Получить заказы (с пагинацией)", description = "Возвращает заказы с пагинацией и общим количеством")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Страница заказов успешно получена")
    })
    @GetMapping("/paged")
    public Mono<ResponseEntity<Page<OrderDto>>> getOrdersPaged(
            @Parameter(description = "Номер страницы (начинается с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size,
            ServerWebExchange exchange) {
        int validatedSize = Math.min(Math.max(size, 1), PaginationUtil.MAX_PAGE_SIZE);
        return orderService.getAllOrdersPaginated(page, validatedSize)
            .map(pagedOrders -> {
                exchange.getResponse().getHeaders().add("X-Total-Count", String.valueOf(pagedOrders.getTotalElements()));
                return ResponseEntity.ok(pagedOrders);
            });
    }

    @Operation(summary = "Получить заказы (бесконечная прокрутка)", description = "Возвращает заказы порциями без общего количества")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Порция заказов успешно получена")
    })
    @GetMapping("/infinite-scroll")
    public Mono<ResponseEntity<Slice<OrderDto>>> getOrdersSlice(
            @Parameter(description = "Номер страницы (начинается с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size) {
        int validatedSize = Math.min(Math.max(size, 1), PaginationUtil.MAX_PAGE_SIZE);
        return orderService.getAllOrdersSlice(page, validatedSize)
            .map(ResponseEntity::ok);
    }

    @Operation(
        summary = "Добавить позицию в заказ",
        description = "Добавляет блюдо в существующий заказ",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Минимальный payload для добавления блюда",
            content = @Content(
                schema = @Schema(implementation = OrderItemDto.class),
                examples = @ExampleObject(
                    name = "Минимальный запрос",
                    summary = "Только обязательные поля",
                    value = """
                        {
                          "dishId": 12,
                          "quantity": 2,
                          "specialRequest": "Без сыра"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Позиция успешно добавлена",
            content = @Content(schema = @Schema(implementation = OrderDto.class))),
        @ApiResponse(responseCode = "400", description = "Некорректные данные запроса",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Заказ или блюдо не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "Сервис меню недоступен",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/items")
    public Mono<OrderDto> addItemToOrder(
            @Parameter(description = "ID заказа", required = true) @PathVariable Long id,
            @Valid @RequestBody OrderItemDto itemDto) {
        return orderService.addItemToOrder(id, itemDto);
    }

    @Operation(summary = "Удалить позицию из заказа", description = "Удаляет блюдо из заказа")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Позиция успешно удалена"),
        @ApiResponse(responseCode = "404", description = "Заказ или позиция не найдены",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeItemFromOrder(
            @Parameter(description = "ID заказа", required = true) @PathVariable Long id,
            @Parameter(description = "ID позиции заказа", required = true) @PathVariable Long itemId) {
        return orderService.removeItemFromOrder(id, itemId);
    }

    @Operation(summary = "Отправить заказ на кухню", description = "Отправляет заказ на кухню для приготовления")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Заказ успешно отправлен на кухню",
            content = @Content(schema = @Schema(implementation = OrderDto.class))),
        @ApiResponse(responseCode = "404", description = "Заказ не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Заказ не в статусе CREATED",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "Сервис кухни недоступен",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/send-to-kitchen")
    public Mono<OrderDto> sendToKitchen(
            @Parameter(description = "ID заказа", required = true) @PathVariable Long id) {
        return orderService.sendToKitchen(id);
    }

    @Operation(summary = "Закрыть заказ", description = "Закрывает заказ и освобождает стол")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Заказ успешно закрыт",
            content = @Content(schema = @Schema(implementation = OrderDto.class))),
        @ApiResponse(responseCode = "404", description = "Заказ не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/close")
    public Mono<OrderDto> closeOrder(
            @Parameter(description = "ID заказа", required = true) @PathVariable Long id) {
        return orderService.closeOrder(id);
    }

    @Operation(summary = "Получить все столы (с пагинацией)", description = "Возвращает список столов с пагинацией")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Страница столов успешно получена")
    })
    @GetMapping("/tables/paged")
    public Mono<ResponseEntity<Page<TableDto>>> getTablesPaged(
            @Parameter(description = "Номер страницы (начинается с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size,
            ServerWebExchange exchange) {
        int validatedSize = Math.min(Math.max(size, 1), PaginationUtil.MAX_PAGE_SIZE);
        return orderService.getAllTablesPaginated(page, validatedSize)
            .map(pagedTables -> {
                exchange.getResponse().getHeaders().add("X-Total-Count", String.valueOf(pagedTables.getTotalElements()));
                return ResponseEntity.ok(pagedTables);
            });
    }

    @Operation(summary = "Получить всех сотрудников (с пагинацией)", description = "Возвращает список сотрудников с пагинацией")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Страница сотрудников успешно получена")
    })
    @GetMapping("/employees/paged")
    public Mono<ResponseEntity<Page<EmployeeDto>>> getEmployeesPaged(
            @Parameter(description = "Номер страницы (начинается с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size,
            ServerWebExchange exchange) {
        int validatedSize = Math.min(Math.max(size, 1), PaginationUtil.MAX_PAGE_SIZE);
        return orderService.getAllEmployeesPaginated(page, validatedSize)
            .map(pagedEmployees -> {
                exchange.getResponse().getHeaders().add("X-Total-Count", String.valueOf(pagedEmployees.getTotalElements()));
                return ResponseEntity.ok(pagedEmployees);
            });
    }
}
