package ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.menu.domain.entity.Ingredient;

import static org.junit.jupiter.api.Assertions.*;

class IngredientJpaEntityTest {

    @Test
    void fromDomain_ShouldMapAllFields() {
        Ingredient domain = Ingredient.builder()
                .id(1L)
                .name("Tomato")
                .unit("kg")
                .build();

        IngredientJpaEntity entity = IngredientJpaEntity.fromDomain(domain);

        assertEquals(1L, entity.getId());
        assertEquals("Tomato", entity.getName());
        assertEquals("kg", entity.getUnit());
    }

    @Test
    void toDomain_ShouldMapAllFields() {
        IngredientJpaEntity entity = IngredientJpaEntity.builder()
                .id(1L)
                .name("Onion")
                .unit("pieces")
                .build();

        Ingredient domain = entity.toDomain();

        assertEquals(1L, domain.getId());
        assertEquals("Onion", domain.getName());
        assertEquals("pieces", domain.getUnit());
    }

    @Test
    void fromDomainAndToDomain_ShouldBeSymmetric() {
        Ingredient original = Ingredient.builder()
                .id(5L)
                .name("Garlic")
                .unit("cloves")
                .build();

        IngredientJpaEntity entity = IngredientJpaEntity.fromDomain(original);
        Ingredient converted = entity.toDomain();

        assertEquals(original.getId(), converted.getId());
        assertEquals(original.getName(), converted.getName());
        assertEquals(original.getUnit(), converted.getUnit());
    }

    @Test
    void builder_ShouldCreateEntity() {
        IngredientJpaEntity entity = IngredientJpaEntity.builder()
                .id(1L)
                .name("Pepper")
                .build();

        assertEquals(1L, entity.getId());
        assertEquals("Pepper", entity.getName());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        IngredientJpaEntity entity = new IngredientJpaEntity();
        entity.setId(1L);
        entity.setName("Salt");
        entity.setUnit("g");

        assertEquals(1L, entity.getId());
        assertEquals("Salt", entity.getName());
        assertEquals("g", entity.getUnit());
    }
}
