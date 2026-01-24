package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemJpaEntityTest {

    @Test
    void constructor_ShouldSetAllFields() {
        OrderItemJpaEntity entity = new OrderItemJpaEntity(
                1L, 2L, 3L, "Margherita Pizza", 2, new BigDecimal("15.99"), "Extra cheese"
        );

        assertEquals(1L, entity.getId());
        assertEquals(2L, entity.getOrderId());
        assertEquals(3L, entity.getDishId());
        assertEquals("Margherita Pizza", entity.getDishName());
        assertEquals(2, entity.getQuantity());
        assertEquals(new BigDecimal("15.99"), entity.getPrice());
        assertEquals("Extra cheese", entity.getSpecialRequest());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.setId(1L);
        entity.setOrderId(5L);
        entity.setDishId(10L);
        entity.setDishName("Caesar Salad");
        entity.setQuantity(1);
        entity.setPrice(new BigDecimal("12.50"));
        entity.setSpecialRequest("No croutons");

        assertEquals(1L, entity.getId());
        assertEquals(5L, entity.getOrderId());
        assertEquals(10L, entity.getDishId());
        assertEquals("Caesar Salad", entity.getDishName());
        assertEquals(1, entity.getQuantity());
        assertEquals(new BigDecimal("12.50"), entity.getPrice());
        assertEquals("No croutons", entity.getSpecialRequest());
    }

    @Test
    void defaultConstructor_ShouldCreateEmptyEntity() {
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        assertNull(entity.getId());
        assertNull(entity.getOrderId());
        assertNull(entity.getDishName());
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        OrderItemJpaEntity entity1 = new OrderItemJpaEntity(
                1L, 2L, 3L, "Pizza", 1, new BigDecimal("10.00"), null
        );
        OrderItemJpaEntity entity2 = new OrderItemJpaEntity(
                1L, 2L, 3L, "Pizza", 1, new BigDecimal("10.00"), null
        );

        assertEquals(entity1, entity2);
    }
}
