package ru.ifmo.se.restaurant.menu;

import ru.ifmo.se.restaurant.menu.dto.CategoryDto;
import ru.ifmo.se.restaurant.menu.dto.DishDto;
import ru.ifmo.se.restaurant.menu.dto.IngredientDto;
import ru.ifmo.se.restaurant.menu.entity.Category;
import ru.ifmo.se.restaurant.menu.entity.Dish;
import ru.ifmo.se.restaurant.menu.entity.Ingredient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TestDataFactory {

    public static Category createMockCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Test Category " + id);
        category.setDescription("Test Category Description");
        category.setIsActive(true);
        return category;
    }

    public static Category createMockCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription("Test Category Description");
        category.setIsActive(true);
        return category;
    }

    public static CategoryDto createMockCategoryDto(Long id) {
        return new CategoryDto(
            id,
            "Test Category " + id,
            "Test Category Description",
            true
        );
    }

    public static CategoryDto createMockCategoryDto(Long id, String name) {
        return new CategoryDto(
            id,
            name,
            "Test Category Description",
            true
        );
    }

    public static Ingredient createMockIngredient(Long id) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(id);
        ingredient.setName("Test Ingredient " + id);
        ingredient.setUnit("kg");
        return ingredient;
    }

    public static Ingredient createMockIngredient(Long id, String name) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(id);
        ingredient.setName(name);
        ingredient.setUnit("kg");
        return ingredient;
    }

    public static IngredientDto createMockIngredientDto(Long id) {
        return new IngredientDto(
            id,
            "Test Ingredient " + id,
            "kg"
        );
    }

    public static IngredientDto createMockIngredientDto(Long id, String name) {
        return new IngredientDto(
            id,
            name,
            "kg"
        );
    }

    public static Dish createMockDish(Long id) {
        Category category = createMockCategory(1L);
        Dish dish = new Dish();
        dish.setId(id);
        dish.setName("Test Dish " + id);
        dish.setDescription("Test Dish Description");
        dish.setPrice(new BigDecimal("25.99"));
        dish.setCost(new BigDecimal("10.00"));
        dish.setCategory(category);
        dish.setIsActive(true);
        dish.setIngredients(new HashSet<>());
        return dish;
    }

    public static Dish createMockDish(Long id, Category category) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setName("Test Dish " + id);
        dish.setDescription("Test Dish Description");
        dish.setPrice(new BigDecimal("25.99"));
        dish.setCost(new BigDecimal("10.00"));
        dish.setCategory(category);
        dish.setIsActive(true);
        dish.setIngredients(new HashSet<>());
        return dish;
    }

    public static Dish createMockDishWithIngredients(Long id, Category category, List<Ingredient> ingredients) {
        Dish dish = createMockDish(id, category);
        dish.getIngredients().addAll(ingredients);
        return dish;
    }

    public static DishDto createMockDishDto(Long id) {
        return new DishDto(
            id,
            "Test Dish " + id,
            "Test Dish Description",
            new BigDecimal("25.99"),
            new BigDecimal("10.00"),
            1L,
            "Test Category 1",
            true,
            new ArrayList<>()
        );
    }

    public static DishDto createMockDishDto(Long id, Long categoryId, String categoryName) {
        return new DishDto(
            id,
            "Test Dish " + id,
            "Test Dish Description",
            new BigDecimal("25.99"),
            new BigDecimal("10.00"),
            categoryId,
            categoryName,
            true,
            new ArrayList<>()
        );
    }

    public static DishDto createMockDishDto(Long id, Long categoryId, String categoryName, List<Long> ingredientIds) {
        return new DishDto(
            id,
            "Test Dish " + id,
            "Test Dish Description",
            new BigDecimal("25.99"),
            new BigDecimal("10.00"),
            categoryId,
            categoryName,
            true,
            ingredientIds
        );
    }

    public static Dish createMockDishWithIsActive(Long id, Boolean isActive) {
        Dish dish = createMockDish(id);
        dish.setIsActive(isActive);
        return dish;
    }

    public static Category createMockCategoryWithIsActive(Long id, Boolean isActive) {
        Category category = createMockCategory(id);
        category.setIsActive(isActive);
        return category;
    }
}
