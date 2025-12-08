package ru.ifmo.se.restaurant.menu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.menu.dto.CategoryDto;
import ru.ifmo.se.restaurant.menu.dto.DishDto;
import ru.ifmo.se.restaurant.menu.dto.IngredientDto;
import ru.ifmo.se.restaurant.menu.service.MenuService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    // Category endpoints
    @PostMapping("/categories")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto dto) {
        return new ResponseEntity<>(menuService.createCategory(dto), HttpStatus.CREATED);
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getCategoryById(id));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(menuService.getAllCategories());
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto dto) {
        return ResponseEntity.ok(menuService.updateCategory(id, dto));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        menuService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // Dish endpoints
    @PostMapping("/dishes")
    public ResponseEntity<DishDto> createDish(@Valid @RequestBody DishDto dto) {
        return new ResponseEntity<>(menuService.createDish(dto), HttpStatus.CREATED);
    }

    @GetMapping("/dishes/{id}")
    public ResponseEntity<DishDto> getDishById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getDishById(id));
    }

    @GetMapping("/dishes")
    public ResponseEntity<Page<DishDto>> getAllDishes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(menuService.getAllDishes(pageable));
    }

    @GetMapping("/dishes/active")
    public ResponseEntity<List<DishDto>> getActiveDishes() {
        return ResponseEntity.ok(menuService.getActiveDishes());
    }

    @PutMapping("/dishes/{id}")
    public ResponseEntity<DishDto> updateDish(
            @PathVariable Long id,
            @Valid @RequestBody DishDto dto) {
        return ResponseEntity.ok(menuService.updateDish(id, dto));
    }

    @DeleteMapping("/dishes/{id}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long id) {
        menuService.deleteDish(id);
        return ResponseEntity.noContent().build();
    }

    // Ingredient endpoints
    @PostMapping("/ingredients")
    public ResponseEntity<IngredientDto> createIngredient(@Valid @RequestBody IngredientDto dto) {
        return new ResponseEntity<>(menuService.createIngredient(dto), HttpStatus.CREATED);
    }

    @GetMapping("/ingredients/{id}")
    public ResponseEntity<IngredientDto> getIngredientById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getIngredientById(id));
    }

    @GetMapping("/ingredients")
    public ResponseEntity<List<IngredientDto>> getAllIngredients() {
        return ResponseEntity.ok(menuService.getAllIngredients());
    }
}
