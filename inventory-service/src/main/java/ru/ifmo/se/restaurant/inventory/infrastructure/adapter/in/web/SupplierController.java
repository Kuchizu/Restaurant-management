package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.inventory.application.dto.ErrorResponse;
import ru.ifmo.se.restaurant.inventory.application.dto.SupplierDto;
import ru.ifmo.se.restaurant.inventory.application.dto.SupplyOrderDto;
import ru.ifmo.se.restaurant.inventory.application.port.in.ManageSupplierUseCase;
import ru.ifmo.se.restaurant.inventory.application.port.in.ManageSupplyOrderUseCase;
import ru.ifmo.se.restaurant.inventory.domain.valueobject.SupplyOrderStatus;

import java.util.List;

@Hidden
@Tag(name = "Suppliers", description = "API для управления поставщиками и заказами на поставку")
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final ManageSupplierUseCase manageSupplierUseCase;
    private final ManageSupplyOrderUseCase manageSupplyOrderUseCase;

    @Operation(summary = "Получить всех поставщиков", description = "Возвращает список всех поставщиков без пагинации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список поставщиков успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupplierDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<SupplierDto>> getAllSuppliers() {
        return ResponseEntity.ok(manageSupplierUseCase.getAllSuppliers());
    }

    @Operation(summary = "Получить всех поставщиков с пагинацией и total count",
            description = "Возвращает страницу поставщиков с информацией об общем количестве в заголовке X-Total-Count. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Страница поставщиков успешно получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/paged")
    public ResponseEntity<Page<SupplierDto>> getAllSuppliersPaged(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Page<SupplierDto> suppliers = manageSupplierUseCase.getAllSuppliersPaginated(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(suppliers.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(suppliers.getTotalPages()));
        headers.add("X-Page-Number", String.valueOf(suppliers.getNumber()));
        headers.add("X-Page-Size", String.valueOf(suppliers.getSize()));

        return ResponseEntity.ok().headers(headers).body(suppliers);
    }

    @Operation(summary = "Получить поставщиков для бесконечной прокрутки (без total count)",
            description = "Возвращает срез поставщиков для реализации бесконечной прокрутки без подсчета общего количества. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Срез поставщиков успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Slice.class)))
    })
    @GetMapping("/infinite-scroll")
    public ResponseEntity<Slice<SupplierDto>> getAllSuppliersInfiniteScroll(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Slice<SupplierDto> suppliers = manageSupplierUseCase.getAllSuppliersSlice(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Has-Next", String.valueOf(suppliers.hasNext()));
        headers.add("X-Has-Previous", String.valueOf(suppliers.hasPrevious()));
        headers.add("X-Page-Number", String.valueOf(suppliers.getNumber()));
        headers.add("X-Page-Size", String.valueOf(suppliers.getSize()));

        return ResponseEntity.ok().headers(headers).body(suppliers);
    }

    @Operation(summary = "Получить поставщика по ID", description = "Возвращает поставщика по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Поставщик найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupplierDto.class))),
            @ApiResponse(responseCode = "404", description = "Поставщик не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<SupplierDto> getSupplierById(
            @Parameter(description = "ID поставщика", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(manageSupplierUseCase.getSupplierById(id));
    }

    @Operation(
        summary = "Создать нового поставщика",
        description = "Создает нового поставщика в системе",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Только обязательные поля",
            content = @Content(
                schema = @Schema(implementation = SupplierDto.class),
                examples = @ExampleObject(
                    name = "Минимальный запрос",
                    value = """
                        {
                          "name": "ООО \\"Поставщик\\""
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Поставщик успешно создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupplierDto.class)))
    })
    @PostMapping
    public ResponseEntity<SupplierDto> createSupplier(
            @Parameter(description = "Данные поставщика", required = true)
            @RequestBody SupplierDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(manageSupplierUseCase.createSupplier(dto));
    }

    @Operation(summary = "Обновить поставщика", description = "Обновляет данные поставщика")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Поставщик успешно обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupplierDto.class))),
            @ApiResponse(responseCode = "404", description = "Поставщик не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<SupplierDto> updateSupplier(
            @Parameter(description = "ID поставщика", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Обновленные данные поставщика", required = true)
            @RequestBody SupplierDto dto) {
        return ResponseEntity.ok(manageSupplierUseCase.updateSupplier(id, dto));
    }

    @Operation(summary = "Удалить поставщика", description = "Полностью удаляет поставщика из системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Поставщик успешно удален"),
            @ApiResponse(responseCode = "404", description = "Поставщик не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(
            @Parameter(description = "ID поставщика", required = true, example = "1")
            @PathVariable Long id) {
        manageSupplierUseCase.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить все заказы на поставку", description = "Возвращает список всех заказов на поставку без пагинации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заказов на поставку успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupplyOrderDto.class)))
    })
    @GetMapping("/orders")
    public ResponseEntity<List<SupplyOrderDto>> getAllSupplyOrders() {
        return ResponseEntity.ok(manageSupplyOrderUseCase.getAllSupplyOrders());
    }

    @Operation(summary = "Получить все заказы на поставку с пагинацией и total count",
            description = "Возвращает страницу заказов на поставку с информацией об общем количестве в заголовке X-Total-Count. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Страница заказов на поставку успешно получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/orders/paged")
    public ResponseEntity<Page<SupplyOrderDto>> getAllSupplyOrdersPaged(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Page<SupplyOrderDto> orders = manageSupplyOrderUseCase.getAllSupplyOrdersPaginated(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(orders.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(orders.getTotalPages()));
        headers.add("X-Page-Number", String.valueOf(orders.getNumber()));
        headers.add("X-Page-Size", String.valueOf(orders.getSize()));

        return ResponseEntity.ok().headers(headers).body(orders);
    }

    @Operation(summary = "Получить заказы на поставку для бесконечной прокрутки (без total count)",
            description = "Возвращает срез заказов на поставку для реализации бесконечной прокрутки без подсчета общего количества. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Срез заказов на поставку успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Slice.class)))
    })
    @GetMapping("/orders/infinite-scroll")
    public ResponseEntity<Slice<SupplyOrderDto>> getAllSupplyOrdersInfiniteScroll(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Slice<SupplyOrderDto> orders = manageSupplyOrderUseCase.getAllSupplyOrdersSlice(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Has-Next", String.valueOf(orders.hasNext()));
        headers.add("X-Has-Previous", String.valueOf(orders.hasPrevious()));
        headers.add("X-Page-Number", String.valueOf(orders.getNumber()));
        headers.add("X-Page-Size", String.valueOf(orders.getSize()));

        return ResponseEntity.ok().headers(headers).body(orders);
    }

    @Operation(summary = "Получить заказ на поставку по ID", description = "Возвращает заказ на поставку по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заказ на поставку найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupplyOrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Заказ на поставку не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/orders/{id}")
    public ResponseEntity<SupplyOrderDto> getSupplyOrderById(
            @Parameter(description = "ID заказа на поставку", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(manageSupplyOrderUseCase.getSupplyOrderById(id));
    }

    @Operation(summary = "Получить заказы на поставку по статусу", description = "Возвращает список заказов на поставку с указанным статусом")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заказов на поставку успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupplyOrderDto.class)))
    })
    @GetMapping("/orders/status/{status}")
    public ResponseEntity<List<SupplyOrderDto>> getSupplyOrdersByStatus(
            @Parameter(description = "Статус заказа на поставку", required = true, example = "PENDING")
            @PathVariable SupplyOrderStatus status) {
        return ResponseEntity.ok(manageSupplyOrderUseCase.getSupplyOrdersByStatus(status));
    }

    @Operation(
        summary = "Создать новый заказ на поставку",
        description = "Создает новый заказ на поставку с указанными ингредиентами и автоматическим расчетом общей стоимости",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Минимальный набор полей",
            content = @Content(
                schema = @Schema(implementation = SupplyOrderDto.class),
                examples = @ExampleObject(
                    name = "Минимальный запрос",
                    value = """
                        {
                          "supplierId": 3,
                          "items": [
                            {
                              "ingredientId": 8,
                              "quantity": 50,
                              "unitPrice": 950
                            }
                          ]
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Заказ на поставку успешно создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupplyOrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Поставщик или ингредиент не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/orders")
    public ResponseEntity<SupplyOrderDto> createSupplyOrder(
            @Parameter(description = "Данные заказа на поставку", required = true)
            @RequestBody SupplyOrderDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(manageSupplyOrderUseCase.createSupplyOrder(dto));
    }

    @Operation(summary = "Обновить статус заказа на поставку",
            description = "Обновляет статус заказа на поставку. При статусе DELIVERED автоматически обновляет инвентарь")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус заказа на поставку успешно обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupplyOrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Заказ на поставку не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<SupplyOrderDto> updateSupplyOrderStatus(
            @Parameter(description = "ID заказа на поставку", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Новый статус", required = true, example = "DELIVERED")
            @RequestParam SupplyOrderStatus status) {
        return ResponseEntity.ok(manageSupplyOrderUseCase.updateSupplyOrderStatus(id, status));
    }

    @Operation(summary = "Удалить заказ на поставку", description = "Полностью удаляет заказ на поставку из системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Заказ на поставку успешно удален"),
            @ApiResponse(responseCode = "404", description = "Заказ на поставку не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteSupplyOrder(
            @Parameter(description = "ID заказа на поставку", required = true, example = "1")
            @PathVariable Long id) {
        manageSupplyOrderUseCase.deleteSupplyOrder(id);
        return ResponseEntity.noContent().build();
    }
}
