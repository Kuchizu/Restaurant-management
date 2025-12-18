package ru.ifmo.se.restaurant.menu.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.ifmo.se.restaurant.menu.dataaccess.CategoryDataAccess;
import ru.ifmo.se.restaurant.menu.dataaccess.DishDataAccess;
import ru.ifmo.se.restaurant.menu.dataaccess.IngredientDataAccess;
import ru.ifmo.se.restaurant.menu.dto.CategoryDto;
import ru.ifmo.se.restaurant.menu.dto.DishDto;
import ru.ifmo.se.restaurant.menu.dto.IngredientDto;
import ru.ifmo.se.restaurant.menu.entity.Category;
import ru.ifmo.se.restaurant.menu.entity.Dish;
import ru.ifmo.se.restaurant.menu.entity.Ingredient;
import ru.ifmo.se.restaurant.menu.exception.ResourceNotFoundException;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.menu.TestDataFactory.*;

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

    // Category Tests
    @Test
    void createCategory_SavesCategoryAndReturnsDto() {
        // Given
        CategoryDto inputDto = createMockCategoryDto(null);
        Category savedCategory = createMockCategory(1L);
        when(categoryDataAccess.save(any(Category.class))).thenReturn(savedCategory);

        // When
        CategoryDto result = menuService.createCategory(inputDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(savedCategory.getName());
        verify(categoryDataAccess).save(any(Category.class));
    }

    @Test
    void getCategoryById_WhenExists_ReturnsCategoryDto() {
        // Given
        Long categoryId = 1L;
        Category category = createMockCategory(categoryId);
        when(categoryDataAccess.getById(categoryId)).thenReturn(category);

        // When
        CategoryDto result = menuService.getCategoryById(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        verify(categoryDataAccess).getById(categoryId);
    }

    @Test
    void getCategoryById_WhenNotExists_ThrowsException() {
        // Given
        Long categoryId = 999L;
        when(categoryDataAccess.getById(categoryId)).thenThrow(new ResourceNotFoundException("Category not found with id: " + categoryId));

        // When & Then
        assertThatThrownBy(() -> menuService.getCategoryById(categoryId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryDataAccess).getById(categoryId);
    }

    @Test
    void getAllCategories_ReturnsAllCategoryDtos() {
        // Given
        List<Category> categories = Arrays.asList(
                createMockCategory(1L),
                createMockCategory(2L)
        );
        when(categoryDataAccess.findAll()).thenReturn(categories);

        // When
        List<CategoryDto> result = menuService.getAllCategories();

        // Then
        assertThat(result).hasSize(2);
        verify(categoryDataAccess).findAll();
    }

    @Test
    void updateCategory_WhenExists_UpdatesAndReturnsDto() {
        // Given
        Long categoryId = 1L;
        CategoryDto updateDto = createMockCategoryDto(categoryId, "Updated Category");
        Category existingCategory = createMockCategory(categoryId);
        when(categoryDataAccess.getById(categoryId)).thenReturn(existingCategory);
        when(categoryDataAccess.save(any(Category.class))).thenReturn(existingCategory);

        // When
        CategoryDto result = menuService.updateCategory(categoryId, updateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        verify(categoryDataAccess).getById(categoryId);
        verify(categoryDataAccess).save(any(Category.class));
    }

    @Test
    void deleteCategory_WhenExists_DeletesCategory() {
        // Given
        Long categoryId = 1L;
        when(categoryDataAccess.existsById(categoryId)).thenReturn(true);
        doNothing().when(categoryDataAccess).deleteById(categoryId);

        // When
        menuService.deleteCategory(categoryId);

        // Then
        verify(categoryDataAccess).existsById(categoryId);
        verify(categoryDataAccess).deleteById(categoryId);
    }

    @Test
    void deleteCategory_WhenNotExists_ThrowsException() {
        // Given
        Long categoryId = 999L;
        when(categoryDataAccess.existsById(categoryId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> menuService.deleteCategory(categoryId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryDataAccess).existsById(categoryId);
        verify(categoryDataAccess, never()).deleteById(any());
    }

    // Dish Tests
    @Test
    void createDish_WithValidCategory_SavesDishAndReturnsDto() {
        // Given
        DishDto inputDto = createMockDishDto(null, 1L, "Test Category");
        Category category = createMockCategory(1L);
        Dish savedDish = createMockDish(1L, category);

        when(categoryDataAccess.getById(1L)).thenReturn(category);
        when(dishDataAccess.save(any(Dish.class))).thenReturn(savedDish);

        // When
        DishDto result = menuService.createDish(inputDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCategoryId()).isEqualTo(1L);
        verify(categoryDataAccess).getById(1L);
        verify(dishDataAccess).save(any(Dish.class));
    }

    @Test
    void createDish_WithInvalidCategory_ThrowsException() {
        // Given
        DishDto inputDto = createMockDishDto(null, 999L, "Invalid Category");
        when(categoryDataAccess.getById(999L)).thenThrow(new ResourceNotFoundException("Category not found with id: 999"));

        // When & Then
        assertThatThrownBy(() -> menuService.createDish(inputDto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryDataAccess).getById(999L);
        verify(dishDataAccess, never()).save(any());
    }

    @Test
    void createDish_WithIngredients_SavesDishWithIngredients() {
        // Given
        List<Long> ingredientIds = Arrays.asList(1L, 2L);
        DishDto inputDto = createMockDishDto(null, 1L, "Test Category", ingredientIds);
        Category category = createMockCategory(1L);
        List<Ingredient> ingredients = Arrays.asList(
                createMockIngredient(1L),
                createMockIngredient(2L)
        );
        Dish savedDish = createMockDishWithIngredients(1L, category, ingredients);

        when(categoryDataAccess.getById(1L)).thenReturn(category);
        when(ingredientDataAccess.findAllById(ingredientIds)).thenReturn(ingredients);
        when(dishDataAccess.save(any(Dish.class))).thenReturn(savedDish);

        // When
        DishDto result = menuService.createDish(inputDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getIngredientIds()).hasSize(2);
        verify(categoryDataAccess).getById(1L);
        verify(ingredientDataAccess).findAllById(ingredientIds);
        verify(dishDataAccess).save(any(Dish.class));
    }

    @Test
    void getDishById_WhenExists_ReturnsDishDto() {
        // Given
        Long dishId = 1L;
        Dish dish = createMockDish(dishId);
        when(dishDataAccess.getById(dishId)).thenReturn(dish);

        // When
        DishDto result = menuService.getDishById(dishId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(dishId);
        verify(dishDataAccess).getById(dishId);
    }

    @Test
    void getAllDishes_WithPageable_ReturnsPagedDishDtos() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Dish> dishes = Arrays.asList(
                createMockDish(1L),
                createMockDish(2L)
        );
        Page<Dish> dishPage = new PageImpl<>(dishes, pageable, dishes.size());
        when(dishDataAccess.findAll(pageable)).thenReturn(dishPage);

        // When
        Page<DishDto> result = menuService.getAllDishes(pageable);

        // Then
        assertThat(result).hasSize(2);
        verify(dishDataAccess).findAll(pageable);
    }

    @Test
    void getActiveDishes_ReturnsOnlyActiveDishes() {
        // Given
        List<Dish> activeDishes = Arrays.asList(
                createMockDishWithIsActive(1L, true),
                createMockDishWithIsActive(2L, true)
        );
        when(dishDataAccess.findByIsActive(true)).thenReturn(activeDishes);

        // When
        List<DishDto> result = menuService.getActiveDishes();

        // Then
        assertThat(result).hasSize(2);
        verify(dishDataAccess).findByIsActive(true);
    }

    @Test
    void updateDish_WhenExists_UpdatesAndReturnsDto() {
        // Given
        Long dishId = 1L;
        Long categoryId = 1L;
        Category category = createMockCategory(categoryId);
        DishDto updateDto = createMockDishDto(dishId, categoryId, "Test Category");
        Dish existingDish = createMockDish(dishId, category);

        when(dishDataAccess.getById(dishId)).thenReturn(existingDish);
        when(categoryDataAccess.getById(categoryId)).thenReturn(category);
        when(dishDataAccess.save(any(Dish.class))).thenReturn(existingDish);

        // When
        DishDto result = menuService.updateDish(dishId, updateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(dishId);
        verify(dishDataAccess).getById(dishId);
        verify(categoryDataAccess).getById(categoryId);
        verify(dishDataAccess).save(any(Dish.class));
    }

    @Test
    void deleteDish_WhenExists_DeletesDish() {
        // Given
        Long dishId = 1L;
        when(dishDataAccess.existsById(dishId)).thenReturn(true);
        doNothing().when(dishDataAccess).deleteById(dishId);

        // When
        menuService.deleteDish(dishId);

        // Then
        verify(dishDataAccess).existsById(dishId);
        verify(dishDataAccess).deleteById(dishId);
    }

    @Test
    void deleteDish_WhenNotExists_ThrowsException() {
        // Given
        Long dishId = 999L;
        when(dishDataAccess.existsById(dishId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> menuService.deleteDish(dishId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(dishDataAccess).existsById(dishId);
        verify(dishDataAccess, never()).deleteById(any());
    }

    // Ingredient Tests
    @Test
    void createIngredient_SavesIngredientAndReturnsDto() {
        // Given
        IngredientDto inputDto = createMockIngredientDto(null);
        Ingredient savedIngredient = createMockIngredient(1L);
        when(ingredientDataAccess.save(any(Ingredient.class))).thenReturn(savedIngredient);

        // When
        IngredientDto result = menuService.createIngredient(inputDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(ingredientDataAccess).save(any(Ingredient.class));
    }

    @Test
    void getIngredientById_WhenExists_ReturnsIngredientDto() {
        // Given
        Long ingredientId = 1L;
        Ingredient ingredient = createMockIngredient(ingredientId);
        when(ingredientDataAccess.getById(ingredientId)).thenReturn(ingredient);

        // When
        IngredientDto result = menuService.getIngredientById(ingredientId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ingredientId);
        verify(ingredientDataAccess).getById(ingredientId);
    }

    @Test
    void getAllIngredients_ReturnsAllIngredientDtos() {
        // Given
        List<Ingredient> ingredients = Arrays.asList(
                createMockIngredient(1L),
                createMockIngredient(2L)
        );
        when(ingredientDataAccess.findAll()).thenReturn(ingredients);

        // When
        List<IngredientDto> result = menuService.getAllIngredients();

        // Then
        assertThat(result).hasSize(2);
        verify(ingredientDataAccess).findAll();
    }
}
