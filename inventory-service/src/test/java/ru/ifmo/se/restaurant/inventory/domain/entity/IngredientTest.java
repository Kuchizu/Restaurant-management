package ru.ifmo.se.restaurant.inventory.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IngredientTest {

    @Test
    void builder_ShouldCreateIngredient() {
        Ingredient ingredient = Ingredient.builder()
                .id(1L)
                .name("Salt")
                .unit("kg")
                .description("Table salt")
                .build();

        assertEquals(1L, ingredient.getId());
        assertEquals("Salt", ingredient.getName());
        assertEquals("kg", ingredient.getUnit());
        assertEquals("Table salt", ingredient.getDescription());
    }

    @Test
    void updateInfo_ShouldUpdateAllFields() {
        Ingredient ingredient = Ingredient.builder()
                .id(1L)
                .name("Salt")
                .unit("kg")
                .description("Table salt")
                .build();

        Ingredient updated = ingredient.updateInfo("Sea Salt", "g", "Premium sea salt");

        assertEquals(1L, updated.getId());
        assertEquals("Sea Salt", updated.getName());
        assertEquals("g", updated.getUnit());
        assertEquals("Premium sea salt", updated.getDescription());
    }

    @Test
    void updateInfo_ShouldKeepOriginalWhenNull() {
        Ingredient ingredient = Ingredient.builder()
                .id(1L)
                .name("Salt")
                .unit("kg")
                .description("Table salt")
                .build();

        Ingredient updated = ingredient.updateInfo(null, null, null);

        assertEquals("Salt", updated.getName());
        assertEquals("kg", updated.getUnit());
        assertEquals("Table salt", updated.getDescription());
    }

    @Test
    void updateInfo_ShouldPartiallyUpdate() {
        Ingredient ingredient = Ingredient.builder()
                .id(1L)
                .name("Salt")
                .unit("kg")
                .description("Table salt")
                .build();

        Ingredient updated = ingredient.updateInfo("Sea Salt", null, "Premium salt");

        assertEquals("Sea Salt", updated.getName());
        assertEquals("kg", updated.getUnit());
        assertEquals("Premium salt", updated.getDescription());
    }

    @Test
    void constructor_ShouldCreateIngredient() {
        Ingredient ingredient = new Ingredient(2L, "Pepper", "g", "Black pepper");

        assertEquals(2L, ingredient.getId());
        assertEquals("Pepper", ingredient.getName());
        assertEquals("g", ingredient.getUnit());
        assertEquals("Black pepper", ingredient.getDescription());
    }
}
