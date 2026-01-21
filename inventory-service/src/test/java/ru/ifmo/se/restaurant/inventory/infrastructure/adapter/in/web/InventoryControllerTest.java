package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.ifmo.se.restaurant.inventory.application.dto.IngredientDto;
import ru.ifmo.se.restaurant.inventory.application.dto.InventoryDto;
import ru.ifmo.se.restaurant.inventory.application.port.in.ManageIngredientUseCase;
import ru.ifmo.se.restaurant.inventory.application.port.in.ManageInventoryUseCase;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ManageInventoryUseCase manageInventoryUseCase;

    @Mock
    private ManageIngredientUseCase manageIngredientUseCase;

    @InjectMocks
    private InventoryController inventoryController;

    private InventoryDto testInventory;
    private IngredientDto testIngredient;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
        objectMapper = new ObjectMapper();

        testIngredient = new IngredientDto();
        testIngredient.setId(1L);
        testIngredient.setName("Salt");
        testIngredient.setUnit("kg");
        testIngredient.setDescription("Table salt");

        testInventory = new InventoryDto();
        testInventory.setId(1L);
        testInventory.setIngredientId(1L);
        testInventory.setIngredientName("Salt");
        testInventory.setQuantity(new BigDecimal("100.00"));
        testInventory.setMinQuantity(new BigDecimal("10.00"));
        testInventory.setMaxQuantity(new BigDecimal("500.00"));
    }

    @Test
    void getAllInventory_ShouldReturnList() throws Exception {
        when(manageInventoryUseCase.getAllInventory()).thenReturn(Arrays.asList(testInventory));

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ingredientName").value("Salt"));
    }

    @Test
    void getInventoryById_ShouldReturnInventory() throws Exception {
        when(manageInventoryUseCase.getInventoryById(1L)).thenReturn(testInventory);

        mockMvc.perform(get("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getLowStockInventory_ShouldReturnList() throws Exception {
        when(manageInventoryUseCase.getLowStockInventory()).thenReturn(Arrays.asList(testInventory));

        mockMvc.perform(get("/api/inventory/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void createInventory_ShouldReturn201() throws Exception {
        when(manageInventoryUseCase.createInventory(any())).thenReturn(testInventory);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInventory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ingredientName").value("Salt"));
    }

    @Test
    void updateInventory_ShouldReturnUpdated() throws Exception {
        when(manageInventoryUseCase.updateInventory(eq(1L), any())).thenReturn(testInventory);

        mockMvc.perform(put("/api/inventory/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInventory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void adjustInventory_ShouldReturnAdjusted() throws Exception {
        when(manageInventoryUseCase.adjustInventory(eq(1L), any(BigDecimal.class)))
                .thenReturn(testInventory);

        mockMvc.perform(patch("/api/inventory/1/adjust")
                        .param("quantity", "10.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteInventory_ShouldReturn204() throws Exception {
        doNothing().when(manageInventoryUseCase).deleteInventory(1L);

        mockMvc.perform(delete("/api/inventory/1"))
                .andExpect(status().isNoContent());

        verify(manageInventoryUseCase).deleteInventory(1L);
    }

    @Test
    void getAllIngredients_ShouldReturnList() throws Exception {
        when(manageIngredientUseCase.getAllIngredients()).thenReturn(Arrays.asList(testIngredient));

        mockMvc.perform(get("/api/inventory/ingredients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Salt"));
    }

    @Test
    void getIngredientById_ShouldReturnIngredient() throws Exception {
        when(manageIngredientUseCase.getIngredientById(1L)).thenReturn(testIngredient);

        mockMvc.perform(get("/api/inventory/ingredients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Salt"));
    }

    @Test
    void createIngredient_ShouldReturn201() throws Exception {
        when(manageIngredientUseCase.createIngredient(any())).thenReturn(testIngredient);

        mockMvc.perform(post("/api/inventory/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testIngredient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Salt"));
    }

    @Test
    void updateIngredient_ShouldReturnUpdated() throws Exception {
        when(manageIngredientUseCase.updateIngredient(eq(1L), any())).thenReturn(testIngredient);

        mockMvc.perform(put("/api/inventory/ingredients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testIngredient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Salt"));
    }

    @Test
    void deleteIngredient_ShouldReturn204() throws Exception {
        doNothing().when(manageIngredientUseCase).deleteIngredient(1L);

        mockMvc.perform(delete("/api/inventory/ingredients/1"))
                .andExpect(status().isNoContent());

        verify(manageIngredientUseCase).deleteIngredient(1L);
    }
}
