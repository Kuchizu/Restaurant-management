package ru.ifmo.se.restaurant.menu.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.ifmo.se.restaurant.menu.dto.CategoryDto;
import ru.ifmo.se.restaurant.menu.dto.DishDto;
import ru.ifmo.se.restaurant.menu.dto.IngredientDto;
import ru.ifmo.se.restaurant.menu.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.service.MenuService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.ifmo.se.restaurant.menu.TestDataFactory.*;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    // ========== CATEGORY TESTS ==========

    @Test
    void createCategory_ReturnsCreated() throws Exception {
        CategoryDto savedDto = createMockCategoryDto(1L);
        when(menuService.createCategory(any(CategoryDto.class)))
                .thenReturn(savedDto);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Desserts\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getCategoryById_WhenExists_ReturnsOk() throws Exception {
        CategoryDto categoryDto = createMockCategoryDto(1L);
        when(menuService.getCategoryById(1L)).thenReturn(categoryDto);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getCategoryById_WhenNotExists_ReturnsNotFound() throws Exception {
        when(menuService.getCategoryById(999L))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCategories_ReturnsAllCategories() throws Exception {
        List<CategoryDto> categories = Arrays.asList(
                createMockCategoryDto(1L),
                createMockCategoryDto(2L)
        );
        when(menuService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getCategoriesPaged_ReturnsPagedCategories() throws Exception {
        List<CategoryDto> categories = Arrays.asList(createMockCategoryDto(1L));
        Page<CategoryDto> page = new PageImpl<>(categories);
        when(menuService.getAllCategoriesPaginated(eq(0), eq(20)))
                .thenReturn(page);

        mockMvc.perform(get("/api/categories/paged?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"));
    }

    @Test
    void getCategoriesSlice_ReturnsSlicedCategories() throws Exception {
        List<CategoryDto> categories = Arrays.asList(createMockCategoryDto(1L));
        Slice<CategoryDto> slice = new SliceImpl<>(categories);
        when(menuService.getAllCategoriesSlice(eq(0), eq(20)))
                .thenReturn(slice);

        mockMvc.perform(get("/api/categories/infinite-scroll?page=0&size=20"))
                .andExpect(status().isOk());
    }

    @Test
    void updateCategory_ReturnsUpdatedCategory() throws Exception {
        CategoryDto updatedDto = createMockCategoryDto(1L, "Updated Category");
        when(menuService.updateCategory(eq(1L), any(CategoryDto.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Category\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category"));
    }

    @Test
    void deleteCategory_ReturnsNoContent() throws Exception {
        doNothing().when(menuService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    // ========== DISH TESTS ==========

    @Test
    void createDish_ReturnsCreated() throws Exception {
        DishDto savedDto = createMockDishDto(1L, 1L, "Desserts");
        when(menuService.createDish(any(DishDto.class)))
                .thenReturn(savedDto);

        mockMvc.perform(post("/api/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cake\",\"categoryId\":1,\"price\":10.0,\"available\":true}"))
                .andExpect(status().isCreated());
    }

    @Test
    void getDishById_WhenExists_ReturnsOk() throws Exception {
        DishDto dishDto = createMockDishDto(1L);
        when(menuService.getDishById(1L)).thenReturn(dishDto);

        mockMvc.perform(get("/api/dishes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getDishById_WhenNotExists_ReturnsNotFound() throws Exception {
        when(menuService.getDishById(999L))
                .thenThrow(new ResourceNotFoundException("Dish not found"));

        mockMvc.perform(get("/api/dishes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDishesPaged_ReturnsPagedDishes() throws Exception {
        List<DishDto> dishes = Arrays.asList(createMockDishDto(1L));
        Page<DishDto> page = new PageImpl<>(dishes);
        when(menuService.getAllDishesPaginated(eq(0), eq(20)))
                .thenReturn(page);

        mockMvc.perform(get("/api/dishes/paged?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"));
    }

    @Test
    void getDishesSlice_ReturnsSlicedDishes() throws Exception {
        List<DishDto> dishes = Arrays.asList(createMockDishDto(1L));
        Slice<DishDto> slice = new SliceImpl<>(dishes);
        when(menuService.getAllDishesSlice(eq(0), eq(20)))
                .thenReturn(slice);

        mockMvc.perform(get("/api/dishes/infinite-scroll?page=0&size=20"))
                .andExpect(status().isOk());
    }

    @Test
    void getActiveDishes_ReturnsActiveDishes() throws Exception {
        List<DishDto> dishes = Arrays.asList(createMockDishDto(1L));
        when(menuService.getActiveDishes()).thenReturn(dishes);

        mockMvc.perform(get("/api/dishes/active"))
                .andExpect(status().isOk());
    }

    @Test
    void updateDish_ReturnsUpdatedDish() throws Exception {
        DishDto updatedDto = createMockDishDto(1L);
        when(menuService.updateDish(eq(1L), any(DishDto.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/dishes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Dish\",\"categoryId\":1,\"price\":15.0,\"available\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDish_ReturnsNoContent() throws Exception {
        doNothing().when(menuService).deleteDish(1L);

        mockMvc.perform(delete("/api/dishes/1"))
                .andExpect(status().isNoContent());
    }

    // ========== INGREDIENT TESTS ==========

    @Test
    void createIngredient_ReturnsCreated() throws Exception {
        IngredientDto savedDto = createMockIngredientDto(1L);
        when(menuService.createIngredient(any(IngredientDto.class)))
                .thenReturn(savedDto);

        mockMvc.perform(post("/api/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Flour\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getIngredientById_WhenExists_ReturnsOk() throws Exception {
        IngredientDto ingredientDto = createMockIngredientDto(1L);
        when(menuService.getIngredientById(1L)).thenReturn(ingredientDto);

        mockMvc.perform(get("/api/ingredients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllIngredients_ReturnsAllIngredients() throws Exception {
        List<IngredientDto> ingredients = Arrays.asList(createMockIngredientDto(1L));
        when(menuService.getAllIngredients()).thenReturn(ingredients);

        mockMvc.perform(get("/api/ingredients"))
                .andExpect(status().isOk());
    }
}
