package ru.ifmo.se.restaurant.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.dto.CategoryDto;
import ru.ifmo.se.restaurant.dto.DishDto;
import ru.ifmo.se.restaurant.service.MenuManagementService;

@RestController
@RequestMapping("/api/menu")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Menu Management", description = "API for managing menu categories and dishes")
public class MenuController {
    private final MenuManagementService menuService;

    public MenuController(MenuManagementService menuService) {
        this.menuService = menuService;
    }

    @PostMapping("/categories")
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto dto) {
        return new ResponseEntity<>(menuService.createCategory(dto), HttpStatus.CREATED);
    }

    @GetMapping("/categories/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get category by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<CategoryDto> getCategory(
            @Parameter(description = "Category ID", required = true, example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(menuService.getCategoryById(id));
    }

    @GetMapping("/categories")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Page<CategoryDto>> getAllCategories(
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(menuService.getAllCategories(page, size));
    }

    @PutMapping("/categories/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<CategoryDto> updateCategory(
            @Parameter(description = "Category ID", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody CategoryDto dto) {
        return ResponseEntity.ok(menuService.updateCategory(id, dto));
    }

    @DeleteMapping("/categories/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "No content"),
        @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", required = true, example = "1") @PathVariable Long id) {
        menuService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/dishes")
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new dish")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<DishDto> createDish(@Valid @RequestBody DishDto dto) {
        return new ResponseEntity<>(menuService.createDish(dto), HttpStatus.CREATED);
    }

    @GetMapping("/dishes/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get dish by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<DishDto> getDish(
            @Parameter(description = "Dish ID", required = true, example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(menuService.getDishById(id));
    }

    @GetMapping("/dishes")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all dishes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Page<DishDto>> getAllDishes(
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(menuService.getAllDishes(page, size));
    }

    @GetMapping("/dishes/category/{categoryId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get dishes by category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Page<DishDto>> getDishesByCategory(
            @Parameter(description = "Category ID", required = true, example = "1") @PathVariable Long categoryId,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(menuService.getDishesByCategory(categoryId, page, size));
    }

    @PutMapping("/dishes/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update dish")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<DishDto> updateDish(
            @Parameter(description = "Dish ID", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody DishDto dto) {
        return ResponseEntity.ok(menuService.updateDish(id, dto));
    }

    @DeleteMapping("/dishes/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete dish")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "No content"),
        @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    public ResponseEntity<Void> deleteDish(
            @Parameter(description = "Dish ID", required = true, example = "1") @PathVariable Long id) {
        menuService.deleteDish(id);
        return ResponseEntity.noContent().build();
    }
}

