package ru.ifmo.se.restaurant.inventory.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    private Ingredient createTestIngredient() {
        return Ingredient.builder()
                .id(1L)
                .name("Salt")
                .unit("kg")
                .description("Table salt")
                .build();
    }

    @Test
    void builder_ShouldCreateInventory() {
        Ingredient ingredient = createTestIngredient();
        LocalDateTime now = LocalDateTime.now();

        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(ingredient)
                .quantity(new BigDecimal("100.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(now)
                .build();

        assertEquals(1L, inventory.getId());
        assertEquals(ingredient, inventory.getIngredient());
        assertEquals(new BigDecimal("100.00"), inventory.getQuantity());
        assertEquals(new BigDecimal("10.00"), inventory.getMinQuantity());
        assertEquals(new BigDecimal("500.00"), inventory.getMaxQuantity());
        assertEquals(now, inventory.getLastUpdated());
    }

    @Test
    void isLowStock_ShouldReturnTrue_WhenQuantityBelowMin() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(createTestIngredient())
                .quantity(new BigDecimal("5.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        assertTrue(inventory.isLowStock());
    }

    @Test
    void isLowStock_ShouldReturnFalse_WhenQuantityAboveMin() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(createTestIngredient())
                .quantity(new BigDecimal("50.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        assertFalse(inventory.isLowStock());
    }

    @Test
    void isLowStock_ShouldReturnFalse_WhenQuantityEqualsMin() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(createTestIngredient())
                .quantity(new BigDecimal("10.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        assertFalse(inventory.isLowStock());
    }

    @Test
    void canAdjust_ShouldReturnTrue_WhenResultIsPositive() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(createTestIngredient())
                .quantity(new BigDecimal("100.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        assertTrue(inventory.canAdjust(new BigDecimal("-50.00")));
    }

    @Test
    void canAdjust_ShouldReturnFalse_WhenResultIsNegative() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(createTestIngredient())
                .quantity(new BigDecimal("100.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        assertFalse(inventory.canAdjust(new BigDecimal("-150.00")));
    }

    @Test
    void canAdjust_ShouldReturnTrue_WhenResultIsZero() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(createTestIngredient())
                .quantity(new BigDecimal("100.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        assertTrue(inventory.canAdjust(new BigDecimal("-100.00")));
    }

    @Test
    void adjustQuantity_ShouldReturnNewInventory() {
        Ingredient ingredient = createTestIngredient();
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(ingredient)
                .quantity(new BigDecimal("100.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        Inventory adjusted = inventory.adjustQuantity(new BigDecimal("-30.00"));

        assertEquals(new BigDecimal("70.00"), adjusted.getQuantity());
        assertEquals(1L, adjusted.getId());
        assertEquals(ingredient, adjusted.getIngredient());
    }

    @Test
    void adjustQuantity_ShouldThrow_WhenResultIsNegative() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(createTestIngredient())
                .quantity(new BigDecimal("100.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> inventory.adjustQuantity(new BigDecimal("-150.00")));
    }

    @Test
    void updateQuantity_ShouldReturnNewInventory() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(createTestIngredient())
                .quantity(new BigDecimal("100.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        Inventory updated = inventory.updateQuantity(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("200.00"), updated.getQuantity());
    }

    @Test
    void updateQuantity_ShouldThrow_WhenQuantityIsNegative() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(createTestIngredient())
                .quantity(new BigDecimal("100.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> inventory.updateQuantity(new BigDecimal("-10.00")));
    }

    @Test
    void updateThresholds_ShouldUpdateMinQuantity() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(createTestIngredient())
                .quantity(new BigDecimal("100.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        Inventory updated = inventory.updateThresholds(new BigDecimal("20.00"), null);

        assertEquals(new BigDecimal("20.00"), updated.getMinQuantity());
        assertEquals(new BigDecimal("500.00"), updated.getMaxQuantity());
    }

    @Test
    void updateThresholds_ShouldUpdateMaxQuantity() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(createTestIngredient())
                .quantity(new BigDecimal("100.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        Inventory updated = inventory.updateThresholds(null, new BigDecimal("1000.00"));

        assertEquals(new BigDecimal("10.00"), updated.getMinQuantity());
        assertEquals(new BigDecimal("1000.00"), updated.getMaxQuantity());
    }

    @Test
    void updateThresholds_ShouldUpdateBoth() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(createTestIngredient())
                .quantity(new BigDecimal("100.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        Inventory updated = inventory.updateThresholds(new BigDecimal("25.00"), new BigDecimal("750.00"));

        assertEquals(new BigDecimal("25.00"), updated.getMinQuantity());
        assertEquals(new BigDecimal("750.00"), updated.getMaxQuantity());
    }
}
