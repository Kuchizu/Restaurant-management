package ru.ifmo.se.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.InventoryDto;
import ru.ifmo.se.restaurant.model.entity.Ingredient;
import ru.ifmo.se.restaurant.repository.IngredientRepository;
import ru.ifmo.se.restaurant.service.InventoryService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
class InventoryControllerTest extends BaseIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long inventoryId;
    private Long ingredientId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Create ingredient
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Chicken");
        ingredient.setUnit("kg");
        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        ingredientId = savedIngredient.getId();

        // Create inventory
        InventoryDto inventory = new InventoryDto();
        inventory.setIngredientId(ingredientId);
        inventory.setQuantity(10);
        inventory.setReservedQuantity(0);
        inventory.setExpiryDate(LocalDate.now().plusDays(7));
        InventoryDto created = inventoryService.addInventory(inventory);
        inventoryId = created.getId();
    }

    @Test
    void testAddInventory() throws Exception {
        InventoryDto inventory = new InventoryDto();
        inventory.setIngredientId(ingredientId);
        inventory.setQuantity(5);
        inventory.setReservedQuantity(0);
        inventory.setExpiryDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testGetInventory() throws Exception {
        mockMvc.perform(get("/api/inventory/" + inventoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inventoryId));
    }

    @Test
    void testGetAllInventory() throws Exception {
        mockMvc.perform(get("/api/inventory")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(header().exists("X-Total-Count"));
    }

    @Test
    void testGetExpiringInventory() throws Exception {
        LocalDate expiryDate = LocalDate.now().plusDays(3);
        mockMvc.perform(get("/api/inventory/expiring")
                .param("date", expiryDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testUpdateInventory() throws Exception {
        InventoryDto inventory = new InventoryDto();
        inventory.setIngredientId(ingredientId);
        inventory.setQuantity(15);
        inventory.setReservedQuantity(0);
        inventory.setExpiryDate(LocalDate.now().plusDays(10));

        mockMvc.perform(put("/api/inventory/" + inventoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(15));
    }

    @Test
    void testDeleteInventory() throws Exception {
        mockMvc.perform(delete("/api/inventory/" + inventoryId))
                .andExpect(status().isNoContent());
    }
}

