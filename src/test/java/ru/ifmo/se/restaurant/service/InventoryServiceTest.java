package ru.ifmo.se.restaurant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.InventoryDto;
import ru.ifmo.se.restaurant.model.entity.Ingredient;
import ru.ifmo.se.restaurant.repository.IngredientRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest extends BaseIntegrationTest {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private IngredientRepository ingredientRepository;

    private Long ingredientId;

    @BeforeEach
    void setUp() {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Tomato");
        ingredient.setUnit("kg");
        Ingredient saved = ingredientRepository.save(ingredient);
        ingredientId = saved.getId();
    }

    @Test
    void testAddInventory() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(ingredientId);
        dto.setQuantity(100);
        dto.setReservedQuantity(0);
        dto.setPricePerUnit(new BigDecimal("2.50"));
        dto.setExpiryDate(LocalDate.now().plusDays(7));

        InventoryDto created = inventoryService.addInventory(dto);
        assertNotNull(created.getId());
        assertEquals(100, created.getQuantity());
    }

    @Test
    void testGetInventoryById() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(ingredientId);
        dto.setQuantity(50);
        dto.setReservedQuantity(0);
        dto.setPricePerUnit(new BigDecimal("3.00"));
        dto.setExpiryDate(LocalDate.now().plusDays(5));
        InventoryDto created = inventoryService.addInventory(dto);

        InventoryDto found = inventoryService.getInventoryById(created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals(50, found.getQuantity());
    }

    @Test
    void testUpdateInventory() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(ingredientId);
        dto.setQuantity(75);
        dto.setReservedQuantity(0);
        dto.setPricePerUnit(new BigDecimal("2.00"));
        dto.setExpiryDate(LocalDate.now().plusDays(10));
        InventoryDto created = inventoryService.addInventory(dto);

        created.setQuantity(100);
        InventoryDto updated = inventoryService.updateInventory(created.getId(), created);
        assertEquals(100, updated.getQuantity());
    }
}

