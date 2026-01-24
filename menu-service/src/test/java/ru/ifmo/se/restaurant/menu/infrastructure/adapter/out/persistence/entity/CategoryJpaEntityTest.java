package ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.menu.domain.entity.Category;

import static org.junit.jupiter.api.Assertions.*;

class CategoryJpaEntityTest {

    @Test
    void fromDomain_ShouldMapAllFields() {
        Category domain = Category.builder()
                .id(1L)
                .name("Appetizers")
                .description("Starter dishes")
                .isActive(true)
                .build();

        CategoryJpaEntity entity = CategoryJpaEntity.fromDomain(domain);

        assertEquals(1L, entity.getId());
        assertEquals("Appetizers", entity.getName());
        assertEquals("Starter dishes", entity.getDescription());
        assertTrue(entity.getIsActive());
    }

    @Test
    void toDomain_ShouldMapAllFields() {
        CategoryJpaEntity entity = CategoryJpaEntity.builder()
                .id(1L)
                .name("Main Course")
                .description("Main dishes")
                .isActive(true)
                .build();

        Category domain = entity.toDomain();

        assertEquals(1L, domain.getId());
        assertEquals("Main Course", domain.getName());
        assertEquals("Main dishes", domain.getDescription());
        assertTrue(domain.getIsActive());
    }

    @Test
    void fromDomainAndToDomain_ShouldBeSymmetric() {
        Category original = Category.builder()
                .id(5L)
                .name("Desserts")
                .description("Sweet dishes")
                .isActive(false)
                .build();

        CategoryJpaEntity entity = CategoryJpaEntity.fromDomain(original);
        Category converted = entity.toDomain();

        assertEquals(original.getId(), converted.getId());
        assertEquals(original.getName(), converted.getName());
        assertEquals(original.getDescription(), converted.getDescription());
        assertEquals(original.getIsActive(), converted.getIsActive());
    }

    @Test
    void builder_ShouldCreateEntity() {
        CategoryJpaEntity entity = CategoryJpaEntity.builder()
                .id(1L)
                .name("Beverages")
                .isActive(true)
                .build();

        assertEquals(1L, entity.getId());
        assertEquals("Beverages", entity.getName());
        assertTrue(entity.getIsActive());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        CategoryJpaEntity entity = new CategoryJpaEntity();
        entity.setId(1L);
        entity.setName("Soups");
        entity.setDescription("Hot soups");
        entity.setIsActive(true);

        assertEquals(1L, entity.getId());
        assertEquals("Soups", entity.getName());
        assertEquals("Hot soups", entity.getDescription());
        assertTrue(entity.getIsActive());
    }
}
