package ru.ifmo.se.restaurant.menu.controller;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.menu.dto.CategoryDto;
import ru.ifmo.se.restaurant.menu.dto.DishDto;
import ru.ifmo.se.restaurant.menu.dto.ErrorResponse;
import ru.ifmo.se.restaurant.menu.dto.IngredientDto;
import ru.ifmo.se.restaurant.menu.service.MenuService;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Menu Service", description = "Menu Service - управление меню ресторана")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    // Category endpoints
    @Operation(
        summary = "Создать категорию",
        description = "Создает новую категорию меню",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Только обязательные поля",
            content = @Content(
                schema = @Schema(implementation = CategoryDto.class),
                examples = @ExampleObject(
                    name = "Минимальный запрос",
                    value = """
                        {
                          "name": "Закуски"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Категория успешно создана",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/categories")
    public Mono<ResponseEntity<CategoryDto>> createCategory(@Valid @RequestBody CategoryDto dto) {
        return menuService.createCategory(dto)
                .map(category -> new ResponseEntity<>(category, HttpStatus.CREATED));
    }

    @Operation(summary = "Получить все категории с пагинацией и total count",
            description = "Возвращает страницу категорий с информацией об общем количестве в заголовке X-Total-Count. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Страница категорий успешно получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/categories/paged")
    public Mono<ResponseEntity<Page<CategoryDto>>> getAllCategoriesPaged(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        return menuService.getAllCategoriesPaginated(page, size)
                .map(categories -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Total-Count", String.valueOf(categories.getTotalElements()));
                    headers.add("X-Total-Pages", String.valueOf(categories.getTotalPages()));
                    headers.add("X-Page-Number", String.valueOf(categories.getNumber()));
                    headers.add("X-Page-Size", String.valueOf(categories.getSize()));
                    return ResponseEntity.ok().headers(headers).body(categories);
                });
    }

    @Operation(summary = "Получить категории для бесконечной прокрутки (без total count)",
            description = "Возвращает срез категорий для реализации бесконечной прокрутки без подсчета общего количества. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Срез категорий успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Slice.class)))
    })
    @GetMapping("/categories/infinite-scroll")
    public Mono<ResponseEntity<Slice<CategoryDto>>> getAllCategoriesInfiniteScroll(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        return menuService.getAllCategoriesSlice(page, size)
                .map(categories -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Has-Next", String.valueOf(categories.hasNext()));
                    headers.add("X-Has-Previous", String.valueOf(categories.hasPrevious()));
                    headers.add("X-Page-Number", String.valueOf(categories.getNumber()));
                    headers.add("X-Page-Size", String.valueOf(categories.getSize()));
                    return ResponseEntity.ok().headers(headers).body(categories);
                });
    }

    @Operation(summary = "Получить все категории", description = "Возвращает список всех категорий без пагинации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список категорий успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class)))
    })
    @GetMapping("/categories")
    public Mono<ResponseEntity<List<CategoryDto>>> getAllCategories() {
        return menuService.getAllCategories()
                .collectList()
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Получить категорию по ID", description = "Возвращает категорию по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Категория не найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/categories/{id}")
    public Mono<ResponseEntity<CategoryDto>> getCategoryById(
            @Parameter(description = "ID категории", required = true, example = "1")
            @PathVariable Long id) {
        return menuService.getCategoryById(id)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Обновить категорию", description = "Обновляет существующую категорию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно обновлена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Категория не найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/categories/{id}")
    public Mono<ResponseEntity<CategoryDto>> updateCategory(
            @Parameter(description = "ID категории", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto dto) {
        return menuService.updateCategory(id, dto)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Удалить категорию", description = "Полностью удаляет категорию из системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Категория успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/categories/{id}")
    public Mono<ResponseEntity<Void>> deleteCategory(
            @Parameter(description = "ID категории", required = true, example = "1")
            @PathVariable Long id) {
        return menuService.deleteCategory(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    // Dish endpoints
    @Operation(
        summary = "Создать блюдо",
        description = "Создает новое блюдо в меню",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Только обязательные поля",
            content = @Content(
                schema = @Schema(implementation = DishDto.class),
                examples = @ExampleObject(
                    name = "Минимальный запрос",
                    value = """
                        {
                          "name": "Пицца Маргарита",
                          "price": 12.5,
                          "cost": 5.0,
                          "categoryId": 1
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Блюдо успешно создано",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DishDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Категория не найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/dishes")
    public Mono<ResponseEntity<DishDto>> createDish(@Valid @RequestBody DishDto dto) {
        return menuService.createDish(dto)
                .map(dish -> new ResponseEntity<>(dish, HttpStatus.CREATED));
    }

    @Operation(summary = "Получить блюдо по названию", description = "Возвращает блюдо по указанному названию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Блюдо найдено",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DishDto.class))),
            @ApiResponse(responseCode = "404", description = "Блюдо не найдено",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/dishes/by-name")
    public Mono<ResponseEntity<DishDto>> getDishByName(
            @Parameter(description = "Название блюда", required = true, example = "Стейк")
            @RequestParam("name") String name) {
        return menuService.getDishByName(name)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Получить блюдо по ID", description = "Возвращает блюдо по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Блюдо найдено",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DishDto.class))),
            @ApiResponse(responseCode = "404", description = "Блюдо не найдено",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/dishes/{id}")
    public Mono<ResponseEntity<DishDto>> getDishById(
            @Parameter(description = "ID блюда", required = true, example = "1")
            @PathVariable Long id) {
        return menuService.getDishById(id)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Получить все блюда с пагинацией и total count",
            description = "Возвращает страницу блюд с информацией об общем количестве в заголовке X-Total-Count. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Страница блюд успешно получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/dishes/paged")
    public Mono<ResponseEntity<Page<DishDto>>> getAllDishesPaged(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        return menuService.getAllDishesPaginated(page, size)
                .map(dishes -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Total-Count", String.valueOf(dishes.getTotalElements()));
                    headers.add("X-Total-Pages", String.valueOf(dishes.getTotalPages()));
                    headers.add("X-Page-Number", String.valueOf(dishes.getNumber()));
                    headers.add("X-Page-Size", String.valueOf(dishes.getSize()));
                    return ResponseEntity.ok().headers(headers).body(dishes);
                });
    }

    @Operation(summary = "Получить блюда для бесконечной прокрутки (без total count)",
            description = "Возвращает срез блюд для реализации бесконечной прокрутки без подсчета общего количества. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Срез блюд успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Slice.class)))
    })
    @GetMapping("/dishes/infinite-scroll")
    public Mono<ResponseEntity<Slice<DishDto>>> getAllDishesInfiniteScroll(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        return menuService.getAllDishesSlice(page, size)
                .map(dishes -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Has-Next", String.valueOf(dishes.hasNext()));
                    headers.add("X-Has-Previous", String.valueOf(dishes.hasPrevious()));
                    headers.add("X-Page-Number", String.valueOf(dishes.getNumber()));
                    headers.add("X-Page-Size", String.valueOf(dishes.getSize()));
                    return ResponseEntity.ok().headers(headers).body(dishes);
                });
    }

    @Operation(summary = "Получить активные блюда", description = "Возвращает список всех активных блюд в меню")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список активных блюд успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DishDto.class)))
    })
    @GetMapping("/dishes/active")
    public Mono<ResponseEntity<List<DishDto>>> getActiveDishes() {
        return menuService.getActiveDishes()
                .collectList()
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Обновить блюдо", description = "Обновляет существующее блюдо")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Блюдо успешно обновлено",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DishDto.class))),
            @ApiResponse(responseCode = "404", description = "Блюдо или категория не найдены",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/dishes/{id}")
    public Mono<ResponseEntity<DishDto>> updateDish(
            @Parameter(description = "ID блюда", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody DishDto dto) {
        return menuService.updateDish(id, dto)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Удалить блюдо", description = "Полностью удаляет блюдо из системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Блюдо успешно удалено"),
            @ApiResponse(responseCode = "404", description = "Блюдо не найдено",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/dishes/{id}")
    public Mono<ResponseEntity<Void>> deleteDish(
            @Parameter(description = "ID блюда", required = true, example = "1")
            @PathVariable Long id) {
        return menuService.deleteDish(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    // Ingredient endpoints
    @Operation(
        summary = "Создать ингредиент",
        description = "Создает новый ингредиент",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Только обязательные поля",
            content = @Content(
                schema = @Schema(implementation = IngredientDto.class),
                examples = @ExampleObject(
                    name = "Минимальный запрос",
                    value = """
                        {
                          "name": "Помидоры"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ингредиент успешно создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IngredientDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/ingredients")
    public Mono<ResponseEntity<IngredientDto>> createIngredient(@Valid @RequestBody IngredientDto dto) {
        return menuService.createIngredient(dto)
                .map(ingredient -> new ResponseEntity<>(ingredient, HttpStatus.CREATED));
    }

    @Operation(summary = "Получить ингредиент по ID", description = "Возвращает ингредиент по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ингредиент найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IngredientDto.class))),
            @ApiResponse(responseCode = "404", description = "Ингредиент не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/ingredients/{id}")
    public Mono<ResponseEntity<IngredientDto>> getIngredientById(
            @Parameter(description = "ID ингредиента", required = true, example = "1")
            @PathVariable Long id) {
        return menuService.getIngredientById(id)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Получить все ингредиенты", description = "Возвращает список всех ингредиентов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список ингредиентов успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IngredientDto.class)))
    })
    @GetMapping("/ingredients")
    public Mono<ResponseEntity<List<IngredientDto>>> getAllIngredients() {
        return menuService.getAllIngredients()
                .collectList()
                .map(ResponseEntity::ok);
    }
}
