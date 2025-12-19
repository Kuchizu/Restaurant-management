package ru.ifmo.se.restaurant.menu.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.ifmo.se.restaurant.menu.dataaccess.CategoryDataAccess;
import ru.ifmo.se.restaurant.menu.dataaccess.DishDataAccess;
import ru.ifmo.se.restaurant.menu.dataaccess.IngredientDataAccess;
import ru.ifmo.se.restaurant.menu.dto.CategoryDto;
import ru.ifmo.se.restaurant.menu.dto.DishDto;
import ru.ifmo.se.restaurant.menu.dto.IngredientDto;
import ru.ifmo.se.restaurant.menu.entity.Category;
import ru.ifmo.se.restaurant.menu.entity.Dish;
import ru.ifmo.se.restaurant.menu.entity.Ingredient;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private CategoryDataAccess categoryDataAccess;

    @Mock
    private DishDataAccess dishDataAccess;

    @Mock
    private IngredientDataAccess ingredientDataAccess;

    @InjectMocks
    private MenuService menuService;

    @Test
    void createCategory() {
        Category category = new Category();
        category.setId(1L);
        when(categoryDataAccess.save(any())).thenReturn(category);
        assertNotNull(menuService.createCategory(new CategoryDto()).block());
    }

    @Test
    void getCategoryById() {
        Category category = new Category();
        category.setId(1L);
        when(categoryDataAccess.getById(1L)).thenReturn(category);
        assertNotNull(menuService.getCategoryById(1L).block());
    }

    @Test
    void getAllCategories() {
        when(categoryDataAccess.findAll()).thenReturn(Collections.emptyList());
        assertNotNull(menuService.getAllCategories().collectList().block());
    }

    @Test
    void createDish() {
        Dish dish = new Dish();
        dish.setId(1L);
        Category category = new Category();
        category.setId(1L);
        dish.setCategory(category);
        dish.setPrice(BigDecimal.TEN);
        when(categoryDataAccess.getById(any())).thenReturn(category);
        when(dishDataAccess.save(any())).thenReturn(dish);
        assertNotNull(menuService.createDish(new DishDto()).block());
    }

    @Test
    void getDishById() {
        Dish dish = new Dish();
        dish.setId(1L);
        Category category = new Category();
        category.setId(1L);
        dish.setCategory(category);
        dish.setPrice(BigDecimal.TEN);
        when(dishDataAccess.getById(1L)).thenReturn(dish);
        assertNotNull(menuService.getDishById(1L).block());
    }

    @Test
    void getActiveDishes() {
        when(dishDataAccess.findByIsActive(true)).thenReturn(Collections.emptyList());
        assertNotNull(menuService.getActiveDishes().collectList().block());
    }

    @Test
    void createIngredient() {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        when(ingredientDataAccess.save(any())).thenReturn(ingredient);
        assertNotNull(menuService.createIngredient(new IngredientDto()).block());
    }

    @Test
    void getIngredientById() {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        when(ingredientDataAccess.getById(1L)).thenReturn(ingredient);
        assertNotNull(menuService.getIngredientById(1L).block());
    }

    @Test
    void getAllIngredients() {
        when(ingredientDataAccess.findAll()).thenReturn(Collections.emptyList());
        assertNotNull(menuService.getAllIngredients().collectList().block());
    }

    @Test
    void getAllCategoriesPaginated() {
        org.springframework.data.domain.Page<Category> page = org.springframework.data.domain.Page.empty();
        when(categoryDataAccess.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        assertNotNull(menuService.getAllCategoriesPaginated(0, 20).block());
    }

    @Test
    void getAllCategoriesSlice() {
        org.springframework.data.domain.Slice<Category> slice = new org.springframework.data.domain.SliceImpl<>(Collections.emptyList());
        when(categoryDataAccess.findAllSlice(any(org.springframework.data.domain.Pageable.class))).thenReturn(slice);
        assertNotNull(menuService.getAllCategoriesSlice(0, 20).block());
    }

    @Test
    void getAllDishesPaginated() {
        org.springframework.data.domain.Page<Dish> page = org.springframework.data.domain.Page.empty();
        when(dishDataAccess.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        assertNotNull(menuService.getAllDishesPaginated(0, 20).block());
    }

    @Test
    void getAllDishesSlice() {
        org.springframework.data.domain.Slice<Dish> slice = new org.springframework.data.domain.SliceImpl<>(Collections.emptyList());
        when(dishDataAccess.findAllSlice(any(org.springframework.data.domain.Pageable.class))).thenReturn(slice);
        assertNotNull(menuService.getAllDishesSlice(0, 20).block());
    }
}
