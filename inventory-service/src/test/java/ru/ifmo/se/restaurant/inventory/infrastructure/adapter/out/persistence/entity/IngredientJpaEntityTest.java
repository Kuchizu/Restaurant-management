package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.inventory.domain.entity.Ingredient;

import static org.junit.jupiter.api.Assertions.*;

class IngredientJpaEntityTest {

    @Test
    void fromDomain_ShouldMapAllFields() {
        Ingredient domain = Ingredient.builder()
                .id(1L)
                .name("Salt")
                .unit("kg")
                .description("Table salt")
                .build();

        IngredientJpaEntity entity = IngredientJpaEntity.fromDomain(domain);

        assertEquals(1L, entity.getId());
        assertEquals("Salt", entity.getName());
        assertEquals("kg", entity.getUnit());
        assertEquals("Table salt", entity.getDescription());
    }

    @Test
    void toDomain_ShouldMapAllFields() {
        IngredientJpaEntity entity = IngredientJpaEntity.builder()
                .id(1L)
                .name("Salt")
                .unit("kg")
                .description("Table salt")
                .build();

        Ingredient domain = entity.toDomain();

        assertEquals(1L, domain.getId());
        assertEquals("Salt", domain.getName());
        assertEquals("kg", domain.getUnit());
        assertEquals("Table salt", domain.getDescription());
    }

    @Test
    void fromDomainAndToDomain_ShouldBeSymmetric() {
        Ingredient original = Ingredient.builder()
                .id(5L)
                .name("Pepper")
                .unit("g")
                .description("Black pepper")
                .build();

        IngredientJpaEntity entity = IngredientJpaEntity.fromDomain(original);
        Ingredient converted = entity.toDomain();

        assertEquals(original.getId(), converted.getId());
        assertEquals(original.getName(), converted.getName());
        assertEquals(original.getUnit(), converted.getUnit());
        assertEquals(original.getDescription(), converted.getDescription());
    }

    @Test
    void builder_ShouldCreateEntity() {
        IngredientJpaEntity entity = IngredientJpaEntity.builder()
                .id(1L)
                .name("Sugar")
                .build();

        assertEquals(1L, entity.getId());
        assertEquals("Sugar", entity.getName());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        IngredientJpaEntity entity = new IngredientJpaEntity();
        entity.setId(1L);
        entity.setName("Flour");
        entity.setUnit("kg");
        entity.setDescription("All-purpose flour");

        assertEquals(1L, entity.getId());
        assertEquals("Flour", entity.getName());
        assertEquals("kg", entity.getUnit());
        assertEquals("All-purpose flour", entity.getDescription());
    }
}
