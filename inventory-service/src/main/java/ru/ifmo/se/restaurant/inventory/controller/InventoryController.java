package ru.ifmo.se.restaurant.inventory.controller;

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
import ru.ifmo.se.restaurant.inventory.dto.ErrorResponse;
import ru.ifmo.se.restaurant.inventory.dto.IngredientDto;
import ru.ifmo.se.restaurant.inventory.dto.InventoryDto;
import ru.ifmo.se.restaurant.inventory.service.InventoryService;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Inventory", description = "API для управления запасами и ингредиентами ресторана")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @Operation(summary = "Получить весь инвентарь", description = "Возвращает список всех позиций инвентаря без пагинации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список инвентаря успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventoryDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<InventoryDto>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @Operation(summary = "Получить весь инвентарь с пагинацией и total count",
            description = "Возвращает страницу инвентаря с информацией об общем количестве в заголовке X-Total-Count. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Страница инвентаря успешно получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/paged")
    public ResponseEntity<Page<InventoryDto>> getAllInventoryPaged(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Page<InventoryDto> inventory = inventoryService.getAllInventoryPaginated(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(inventory.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(inventory.getTotalPages()));
        headers.add("X-Page-Number", String.valueOf(inventory.getNumber()));
        headers.add("X-Page-Size", String.valueOf(inventory.getSize()));

        return ResponseEntity.ok().headers(headers).body(inventory);
    }

    @Operation(summary = "Получить инвентарь для бесконечной прокрутки (без total count)",
            description = "Возвращает срез инвентаря для реализации бесконечной прокрутки без подсчета общего количества. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Срез инвентаря успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Slice.class)))
    })
    @GetMapping("/infinite-scroll")
    public ResponseEntity<Slice<InventoryDto>> getAllInventoryInfiniteScroll(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Slice<InventoryDto> inventory = inventoryService.getAllInventorySlice(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Has-Next", String.valueOf(inventory.hasNext()));
        headers.add("X-Has-Previous", String.valueOf(inventory.hasPrevious()));
        headers.add("X-Page-Number", String.valueOf(inventory.getNumber()));
        headers.add("X-Page-Size", String.valueOf(inventory.getSize()));

        return ResponseEntity.ok().headers(headers).body(inventory);
    }

    @Operation(summary = "Получить инвентарь по ID", description = "Возвращает позицию инвентаря по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Инвентарь найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Инвентарь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<InventoryDto> getInventoryById(
            @Parameter(description = "ID инвентаря", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @Operation(summary = "Получить инвентарь с низким запасом", description = "Возвращает список позиций инвентаря с количеством ниже минимального")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список позиций с низким запасом успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventoryDto.class)))
    })
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryDto>> getLowStockInventory() {
        return ResponseEntity.ok(inventoryService.getLowStockInventory());
    }

    @Operation(
        summary = "Создать новую позицию инвентаря",
        description = "Создает новую позицию инвентаря для указанного ингредиента",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Данные для создания позиции инвентаря",
            content = @Content(
                schema = @Schema(implementation = InventoryDto.class),
                examples = @ExampleObject(
                    name = "Полный пример",
                    value = """
                        {
                          "ingredientId": 8,
                          "quantity": 45.5,
                          "minQuantity": 10.0,
                          "maxQuantity": 100.0
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Инвентарь успешно создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Ингредиент не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<InventoryDto> createInventory(
            @Parameter(description = "Данные инвентаря", required = true)
            @RequestBody InventoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createInventory(dto));
    }

    @Operation(summary = "Обновить позицию инвентаря", description = "Обновляет данные позиции инвентаря")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Инвентарь успешно обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Инвентарь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<InventoryDto> updateInventory(
            @Parameter(description = "ID инвентаря", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Обновленные данные инвентаря", required = true)
            @RequestBody InventoryDto dto) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, dto));
    }

    @Operation(summary = "Скорректировать количество", description = "Корректирует количество в инвентаре (добавление или уменьшение)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Количество успешно скорректировано",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Инвентарь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/adjust")
    public ResponseEntity<InventoryDto> adjustInventory(
            @Parameter(description = "ID инвентаря", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Количество для корректировки (может быть отрицательным)", required = true, example = "10.0")
            @RequestParam BigDecimal quantity) {
        return ResponseEntity.ok(inventoryService.adjustInventory(id, quantity));
    }

    @Operation(summary = "Удалить позицию инвентаря", description = "Полностью удаляет позицию инвентаря из системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Инвентарь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Инвентарь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(
            @Parameter(description = "ID инвентаря", required = true, example = "1")
            @PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить все ингредиенты", description = "Возвращает список всех ингредиентов без пагинации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список ингредиентов успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IngredientDto.class)))
    })
    @GetMapping("/ingredients")
    public ResponseEntity<List<IngredientDto>> getAllIngredients() {
        return ResponseEntity.ok(inventoryService.getAllIngredients());
    }

    @Operation(summary = "Получить все ингредиенты с пагинацией и total count",
            description = "Возвращает страницу ингредиентов с информацией об общем количестве в заголовке X-Total-Count. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Страница ингредиентов успешно получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/ingredients/paged")
    public ResponseEntity<Page<IngredientDto>> getAllIngredientsPaged(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Page<IngredientDto> ingredients = inventoryService.getAllIngredientsPaginated(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(ingredients.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(ingredients.getTotalPages()));
        headers.add("X-Page-Number", String.valueOf(ingredients.getNumber()));
        headers.add("X-Page-Size", String.valueOf(ingredients.getSize()));

        return ResponseEntity.ok().headers(headers).body(ingredients);
    }

    @Operation(summary = "Получить ингредиенты для бесконечной прокрутки (без total count)",
            description = "Возвращает срез ингредиентов для реализации бесконечной прокрутки без подсчета общего количества. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Срез ингредиентов успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Slice.class)))
    })
    @GetMapping("/ingredients/infinite-scroll")
    public ResponseEntity<Slice<IngredientDto>> getAllIngredientsInfiniteScroll(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Slice<IngredientDto> ingredients = inventoryService.getAllIngredientsSlice(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Has-Next", String.valueOf(ingredients.hasNext()));
        headers.add("X-Has-Previous", String.valueOf(ingredients.hasPrevious()));
        headers.add("X-Page-Number", String.valueOf(ingredients.getNumber()));
        headers.add("X-Page-Size", String.valueOf(ingredients.getSize()));

        return ResponseEntity.ok().headers(headers).body(ingredients);
    }

    @Operation(summary = "Получить ингредиент по ID", description = "Возвращает ингредиент по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ингредиент найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IngredientDto.class))),
            @ApiResponse(responseCode = "404", description = "Ингредиент не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/ingredients/{id}")
    public ResponseEntity<IngredientDto> getIngredientById(
            @Parameter(description = "ID ингредиента", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getIngredientById(id));
    }

    @Operation(
        summary = "Создать новый ингредиент",
        description = "Создает новый ингредиент в системе",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Данные для создания ингредиента",
            content = @Content(
                schema = @Schema(implementation = IngredientDto.class),
                examples = @ExampleObject(
                    name = "Полный пример",
                    value = """
                        {
                          "name": "Говядина мраморная",
                          "unit": "кг",
                          "description": "Мраморная говядина высшего сорта для стейков"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ингредиент успешно создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IngredientDto.class)))
    })
    @PostMapping("/ingredients")
    public ResponseEntity<IngredientDto> createIngredient(
            @Parameter(description = "Данные ингредиента", required = true)
            @RequestBody IngredientDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createIngredient(dto));
    }

    @Operation(summary = "Обновить ингредиент", description = "Обновляет данные ингредиента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ингредиент успешно обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IngredientDto.class))),
            @ApiResponse(responseCode = "404", description = "Ингредиент не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/ingredients/{id}")
    public ResponseEntity<IngredientDto> updateIngredient(
            @Parameter(description = "ID ингредиента", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Обновленные данные ингредиента", required = true)
            @RequestBody IngredientDto dto) {
        return ResponseEntity.ok(inventoryService.updateIngredient(id, dto));
    }

    @Operation(summary = "Удалить ингредиент", description = "Полностью удаляет ингредиент из системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ингредиент успешно удален"),
            @ApiResponse(responseCode = "404", description = "Ингредиент не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/ingredients/{id}")
    public ResponseEntity<Void> deleteIngredient(
            @Parameter(description = "ID ингредиента", required = true, example = "1")
            @PathVariable Long id) {
        inventoryService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }
}
