package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class KitchenQueueJpaEntityTest {

    @Test
    void fromDomain_ShouldMapAllFields() {
        LocalDateTime now = LocalDateTime.now();
        KitchenQueue domain = KitchenQueue.builder()
                .id(1L)
                .orderId(100L)
                .orderItemId(10L)
                .dishName("Pizza")
                .quantity(2)
                .status(DishStatus.IN_PROGRESS)
                .specialRequest("No onions")
                .createdAt(now)
                .startedAt(now.plusMinutes(5))
                .completedAt(now.plusMinutes(20))
                .build();

        KitchenQueueJpaEntity entity = KitchenQueueJpaEntity.fromDomain(domain);

        assertEquals(1L, entity.getId());
        assertEquals(100L, entity.getOrderId());
        assertEquals(10L, entity.getOrderItemId());
        assertEquals("Pizza", entity.getDishName());
        assertEquals(2, entity.getQuantity());
        assertEquals(DishStatus.IN_PROGRESS, entity.getStatus());
        assertEquals("No onions", entity.getSpecialRequest());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now.plusMinutes(5), entity.getStartedAt());
        assertEquals(now.plusMinutes(20), entity.getCompletedAt());
    }

    @Test
    void toDomain_ShouldMapAllFields() {
        LocalDateTime now = LocalDateTime.now();
        KitchenQueueJpaEntity entity = KitchenQueueJpaEntity.builder()
                .id(1L)
                .orderId(100L)
                .orderItemId(10L)
                .dishName("Burger")
                .quantity(3)
                .status(DishStatus.READY)
                .specialRequest("Extra cheese")
                .createdAt(now)
                .startedAt(now.plusMinutes(2))
                .completedAt(now.plusMinutes(15))
                .build();

        KitchenQueue domain = entity.toDomain();

        assertEquals(1L, domain.getId());
        assertEquals(100L, domain.getOrderId());
        assertEquals(10L, domain.getOrderItemId());
        assertEquals("Burger", domain.getDishName());
        assertEquals(3, domain.getQuantity());
        assertEquals(DishStatus.READY, domain.getStatus());
        assertEquals("Extra cheese", domain.getSpecialRequest());
        assertEquals(now, domain.getCreatedAt());
        assertEquals(now.plusMinutes(2), domain.getStartedAt());
        assertEquals(now.plusMinutes(15), domain.getCompletedAt());
    }

    @Test
    void fromDomain_AndToDomain_ShouldBeSymmetric() {
        LocalDateTime now = LocalDateTime.now();
        KitchenQueue original = KitchenQueue.builder()
                .id(5L)
                .orderId(200L)
                .orderItemId(20L)
                .dishName("Salad")
                .quantity(1)
                .status(DishStatus.PENDING)
                .createdAt(now)
                .build();

        KitchenQueueJpaEntity entity = KitchenQueueJpaEntity.fromDomain(original);
        KitchenQueue result = entity.toDomain();

        assertEquals(original.getId(), result.getId());
        assertEquals(original.getOrderId(), result.getOrderId());
        assertEquals(original.getOrderItemId(), result.getOrderItemId());
        assertEquals(original.getDishName(), result.getDishName());
        assertEquals(original.getQuantity(), result.getQuantity());
        assertEquals(original.getStatus(), result.getStatus());
    }

    @Test
    void builder_ShouldSetDefaultValues() {
        KitchenQueueJpaEntity entity = KitchenQueueJpaEntity.builder()
                .orderId(1L)
                .orderItemId(1L)
                .build();

        assertEquals(1, entity.getQuantity());
        assertEquals(DishStatus.PENDING, entity.getStatus());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void noArgsConstructor_ShouldWork() {
        KitchenQueueJpaEntity entity = new KitchenQueueJpaEntity();
        assertNull(entity.getId());
    }

    @Test
    void allArgsConstructor_ShouldWork() {
        LocalDateTime now = LocalDateTime.now();
        KitchenQueueJpaEntity entity = new KitchenQueueJpaEntity(
                1L, 100L, 10L, "Pasta", 2, DishStatus.IN_PROGRESS,
                "Al dente", now, now.plusMinutes(1), now.plusMinutes(10)
        );

        assertEquals(1L, entity.getId());
        assertEquals("Pasta", entity.getDishName());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        KitchenQueueJpaEntity entity = new KitchenQueueJpaEntity();
        entity.setId(1L);
        entity.setOrderId(100L);
        entity.setOrderItemId(10L);
        entity.setDishName("Soup");
        entity.setQuantity(1);
        entity.setStatus(DishStatus.PENDING);

        assertEquals(1L, entity.getId());
        assertEquals(100L, entity.getOrderId());
        assertEquals(10L, entity.getOrderItemId());
        assertEquals("Soup", entity.getDishName());
        assertEquals(1, entity.getQuantity());
        assertEquals(DishStatus.PENDING, entity.getStatus());
    }
}
