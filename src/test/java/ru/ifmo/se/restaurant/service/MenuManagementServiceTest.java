package ru.ifmo.se.restaurant.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.CategoryDto;
import ru.ifmo.se.restaurant.dto.DishDto;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MenuManagementServiceTest extends BaseIntegrationTest {
    @Autowired
    private MenuManagementService menuService;

    @Test
    void testCreateAndGetCategory() {
        CategoryDto category = new CategoryDto();
        category.setName("Main Course");
        category.setDescription("Main dishes");

        CategoryDto created = menuService.createCategory(category);
        assertNotNull(created.getId());
        assertEquals("Main Course", created.getName());

        CategoryDto found = menuService.getCategoryById(created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals("Main Course", found.getName());
    }

    @Test
    void testGetAllCategories() {
        CategoryDto cat1 = new CategoryDto();
        cat1.setName("Appetizers");
        menuService.createCategory(cat1);

        CategoryDto cat2 = new CategoryDto();
        cat2.setName("Desserts");
        menuService.createCategory(cat2);

        Page<CategoryDto> categories = menuService.getAllCategories(0, 10);
        assertTrue(categories.getTotalElements() >= 2);
    }

    @Test
    void testUpdateCategory() {
        CategoryDto category = new CategoryDto();
        category.setName("Salads");
        CategoryDto created = menuService.createCategory(category);

        created.setDescription("Fresh salads");
        CategoryDto updated = menuService.updateCategory(created.getId(), created);
        assertEquals("Fresh salads", updated.getDescription());
    }

    @Test
    void testDeleteCategory() {
        CategoryDto category = new CategoryDto();
        category.setName("Beverages");
        CategoryDto created = menuService.createCategory(category);

        menuService.deleteCategory(created.getId());
        assertThrows(ResourceNotFoundException.class, () -> menuService.getCategoryById(created.getId()));
    }

    @Test
    void testCreateAndGetDish() {
        CategoryDto category = new CategoryDto();
        category.setName("Main Dishes");
        CategoryDto createdCategory = menuService.createCategory(category);

        DishDto dish = new DishDto();
        dish.setName("Pasta Carbonara");
        dish.setDescription("Creamy pasta");
        dish.setPrice(new BigDecimal("12.50"));
        dish.setCost(new BigDecimal("5.00"));
        dish.setCategoryId(createdCategory.getId());

        DishDto created = menuService.createDish(dish);
        assertNotNull(created.getId());
        assertEquals("Pasta Carbonara", created.getName());

        DishDto found = menuService.getDishById(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void testGetAllDishes() {
        CategoryDto category = new CategoryDto();
        category.setName("Soups");
        CategoryDto createdCategory = menuService.createCategory(category);

        DishDto dish1 = new DishDto();
        dish1.setName("Tomato Soup");
        dish1.setPrice(new BigDecimal("8.00"));
        dish1.setCost(new BigDecimal("3.00"));
        dish1.setCategoryId(createdCategory.getId());
        menuService.createDish(dish1);

        Page<DishDto> dishes = menuService.getAllDishes(0, 10);
        assertTrue(dishes.getTotalElements() >= 1);
    }

    @Test
    void testUpdateDish() {
        CategoryDto category = new CategoryDto();
        category.setName("Starters");
        CategoryDto createdCategory = menuService.createCategory(category);

        DishDto dish = new DishDto();
        dish.setName("Garlic Bread");
        dish.setPrice(new BigDecimal("5.00"));
        dish.setCost(new BigDecimal("2.00"));
        dish.setCategoryId(createdCategory.getId());
        DishDto created = menuService.createDish(dish);

        created.setPrice(new BigDecimal("6.00"));
        created.setDescription("Fresh garlic bread");
        DishDto updated = menuService.updateDish(created.getId(), created);
        assertEquals(new BigDecimal("6.00"), updated.getPrice());
        assertEquals("Fresh garlic bread", updated.getDescription());
    }

    @Test
    void testDeleteDish() {
        CategoryDto category = new CategoryDto();
        category.setName("Sides");
        CategoryDto createdCategory = menuService.createCategory(category);

        DishDto dish = new DishDto();
        dish.setName("French Fries");
        dish.setPrice(new BigDecimal("4.00"));
        dish.setCost(new BigDecimal("1.50"));
        dish.setCategoryId(createdCategory.getId());
        DishDto created = menuService.createDish(dish);

        menuService.deleteDish(created.getId());
        assertThrows(ResourceNotFoundException.class, () -> menuService.getDishById(created.getId()));
    }

    @Test
    void testGetDishesByCategory() {
        CategoryDto category = new CategoryDto();
        category.setName("Pizza");
        CategoryDto createdCategory = menuService.createCategory(category);

        DishDto dish = new DishDto();
        dish.setName("Margherita");
        dish.setPrice(new BigDecimal("10.00"));
        dish.setCost(new BigDecimal("4.00"));
        dish.setCategoryId(createdCategory.getId());
        menuService.createDish(dish);

        Page<DishDto> dishes = menuService.getDishesByCategory(createdCategory.getId(), 0, 10);
        assertFalse(dishes.isEmpty());
    }

    @Test
    void testGetCategoryNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> menuService.getCategoryById(99999L));
    }

    @Test
    void testGetDishNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> menuService.getDishById(99999L));
    }

    @Test
    void testUpdateCategoryNotFound() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Test");
        assertThrows(ResourceNotFoundException.class, () -> menuService.updateCategory(99999L, dto));
    }

    @Test
    void testUpdateDishNotFound() {
        DishDto dto = new DishDto();
        dto.setName("Test");
        assertThrows(ResourceNotFoundException.class, () -> menuService.updateDish(99999L, dto));
    }
}

