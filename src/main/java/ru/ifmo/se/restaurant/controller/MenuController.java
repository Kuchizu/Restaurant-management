package ru.ifmo.se.restaurant.controller;

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
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto dto) {
        return new ResponseEntity<>(menuService.createCategory(dto), HttpStatus.CREATED);
    }

    @GetMapping("/categories/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getCategoryById(id));
    }

    @GetMapping("/categories")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all categories")
    public ResponseEntity<Page<CategoryDto>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(menuService.getAllCategories(page, size));
    }

    @PutMapping("/categories/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update category")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        return ResponseEntity.ok(menuService.updateCategory(id, dto));
    }

    @DeleteMapping("/categories/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete category")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        menuService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/dishes")
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new dish")
    public ResponseEntity<DishDto> createDish(@Valid @RequestBody DishDto dto) {
        return new ResponseEntity<>(menuService.createDish(dto), HttpStatus.CREATED);
    }

    @GetMapping("/dishes/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get dish by ID")
    public ResponseEntity<DishDto> getDish(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getDishById(id));
    }

    @GetMapping("/dishes")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all dishes")
    public ResponseEntity<Page<DishDto>> getAllDishes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(menuService.getAllDishes(page, size));
    }

    @GetMapping("/dishes/category/{categoryId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get dishes by category")
    public ResponseEntity<Page<DishDto>> getDishesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(menuService.getDishesByCategory(categoryId, page, size));
    }

    @PutMapping("/dishes/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update dish")
    public ResponseEntity<DishDto> updateDish(@PathVariable Long id, @Valid @RequestBody DishDto dto) {
        return ResponseEntity.ok(menuService.updateDish(id, dto));
    }

    @DeleteMapping("/dishes/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete dish")
    public ResponseEntity<Void> deleteDish(@PathVariable Long id) {
        menuService.deleteDish(id);
        return ResponseEntity.noContent().build();
    }
}

