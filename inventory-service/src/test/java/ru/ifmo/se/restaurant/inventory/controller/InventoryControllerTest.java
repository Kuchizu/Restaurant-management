package ru.ifmo.se.restaurant.inventory.controller;

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
import ru.ifmo.se.restaurant.inventory.dto.IngredientDto;
import ru.ifmo.se.restaurant.inventory.dto.InventoryDto;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.service.InventoryService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.ifmo.se.restaurant.inventory.TestDataFactory.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    // ========== INVENTORY TESTS ==========

    @Test
    void getAllInventory_ReturnsAllInventory() throws Exception {
        List<InventoryDto> inventory = Arrays.asList(
                createMockInventoryDto(1L),
                createMockInventoryDto(2L)
        );
        when(inventoryService.getAllInventory()).thenReturn(inventory);

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllInventoryPaged_ReturnsPagedInventory() throws Exception {
        List<InventoryDto> inventory = Arrays.asList(createMockInventoryDto(1L));
        Page<InventoryDto> page = new PageImpl<>(inventory);
        when(inventoryService.getAllInventoryPaginated(eq(0), eq(20)))
                .thenReturn(page);

        mockMvc.perform(get("/api/inventory/paged?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(header().exists("X-Total-Pages"))
                .andExpect(header().exists("X-Page-Number"))
                .andExpect(header().exists("X-Page-Size"));
    }

    @Test
    void getAllInventoryInfiniteScroll_ReturnsSlicedInventory() throws Exception {
        List<InventoryDto> inventory = Arrays.asList(createMockInventoryDto(1L));
        Slice<InventoryDto> slice = new SliceImpl<>(inventory);
        when(inventoryService.getAllInventorySlice(eq(0), eq(20)))
                .thenReturn(slice);

        mockMvc.perform(get("/api/inventory/infinite-scroll?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Has-Next"))
                .andExpect(header().exists("X-Has-Previous"))
                .andExpect(header().exists("X-Page-Number"))
                .andExpect(header().exists("X-Page-Size"));
    }

    @Test
    void getInventoryById_WhenExists_ReturnsOk() throws Exception {
        InventoryDto inventoryDto = createMockInventoryDto(1L);
        when(inventoryService.getInventoryById(1L)).thenReturn(inventoryDto);

        mockMvc.perform(get("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getInventoryById_WhenNotExists_ReturnsNotFound() throws Exception {
        when(inventoryService.getInventoryById(999L))
                .thenThrow(new ResourceNotFoundException("Inventory not found"));

        mockMvc.perform(get("/api/inventory/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLowStockInventory_ReturnsLowStockItems() throws Exception {
        List<InventoryDto> lowStock = Arrays.asList(createMockInventoryDto(1L));
        when(inventoryService.getLowStockInventory()).thenReturn(lowStock);

        mockMvc.perform(get("/api/inventory/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void createInventory_ReturnsCreated() throws Exception {
        InventoryDto savedDto = createMockInventoryDto(1L);
        when(inventoryService.createInventory(any(InventoryDto.class)))
                .thenReturn(savedDto);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ingredientId\":1,\"quantity\":100}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateInventory_ReturnsUpdatedInventory() throws Exception {
        InventoryDto updatedDto = createMockInventoryDto(1L);
        when(inventoryService.updateInventory(eq(1L), any(InventoryDto.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/inventory/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":150}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void adjustInventory_ReturnsAdjustedInventory() throws Exception {
        InventoryDto adjustedDto = createMockInventoryDto(1L);
        when(inventoryService.adjustInventory(eq(1L), any(BigDecimal.class)))
                .thenReturn(adjustedDto);

        mockMvc.perform(patch("/api/inventory/1/adjust?quantity=10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteInventory_ReturnsNoContent() throws Exception {
        doNothing().when(inventoryService).deleteInventory(1L);

        mockMvc.perform(delete("/api/inventory/1"))
                .andExpect(status().isNoContent());
    }

    // ========== INGREDIENT TESTS ==========

    @Test
    void getAllIngredients_ReturnsAllIngredients() throws Exception {
        List<IngredientDto> ingredients = Arrays.asList(
                createMockIngredientDto(1L),
                createMockIngredientDto(2L)
        );
        when(inventoryService.getAllIngredients()).thenReturn(ingredients);

        mockMvc.perform(get("/api/inventory/ingredients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllIngredientsPaged_ReturnsPagedIngredients() throws Exception {
        List<IngredientDto> ingredients = Arrays.asList(createMockIngredientDto(1L));
        Page<IngredientDto> page = new PageImpl<>(ingredients);
        when(inventoryService.getAllIngredientsPaginated(eq(0), eq(20)))
                .thenReturn(page);

        mockMvc.perform(get("/api/inventory/ingredients/paged?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(header().exists("X-Total-Pages"))
                .andExpect(header().exists("X-Page-Number"))
                .andExpect(header().exists("X-Page-Size"));
    }

    @Test
    void getAllIngredientsInfiniteScroll_ReturnsSlicedIngredients() throws Exception {
        List<IngredientDto> ingredients = Arrays.asList(createMockIngredientDto(1L));
        Slice<IngredientDto> slice = new SliceImpl<>(ingredients);
        when(inventoryService.getAllIngredientsSlice(eq(0), eq(20)))
                .thenReturn(slice);

        mockMvc.perform(get("/api/inventory/ingredients/infinite-scroll?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Has-Next"))
                .andExpect(header().exists("X-Has-Previous"))
                .andExpect(header().exists("X-Page-Number"))
                .andExpect(header().exists("X-Page-Size"));
    }

    @Test
    void getIngredientById_WhenExists_ReturnsOk() throws Exception {
        IngredientDto ingredientDto = createMockIngredientDto(1L);
        when(inventoryService.getIngredientById(1L)).thenReturn(ingredientDto);

        mockMvc.perform(get("/api/inventory/ingredients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getIngredientById_WhenNotExists_ReturnsNotFound() throws Exception {
        when(inventoryService.getIngredientById(999L))
                .thenThrow(new ResourceNotFoundException("Ingredient not found"));

        mockMvc.perform(get("/api/inventory/ingredients/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createIngredient_ReturnsCreated() throws Exception {
        IngredientDto savedDto = createMockIngredientDto(1L);
        when(inventoryService.createIngredient(any(IngredientDto.class)))
                .thenReturn(savedDto);

        mockMvc.perform(post("/api/inventory/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Flour\",\"unit\":\"kg\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateIngredient_ReturnsUpdatedIngredient() throws Exception {
        IngredientDto updatedDto = createMockIngredientDto(1L);
        when(inventoryService.updateIngredient(eq(1L), any(IngredientDto.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/inventory/ingredients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Flour\",\"unit\":\"kg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteIngredient_ReturnsNoContent() throws Exception {
        doNothing().when(inventoryService).deleteIngredient(1L);

        mockMvc.perform(delete("/api/inventory/ingredients/1"))
                .andExpect(status().isNoContent());
    }
}
