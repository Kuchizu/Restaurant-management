package ru.ifmo.se.restaurant.kitchen.domain.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class KitchenQueueTest {

    @Test
    void builder_ShouldCreateItem() {
        KitchenQueue item = KitchenQueue.builder()
                .id(1L).orderId(100L).orderItemId(10L)
                .dishName("Pizza").quantity(2)
                .status(DishStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        assertEquals(1L, item.getId());
        assertEquals("Pizza", item.getDishName());
    }

    @Test
    void isActive_ShouldReturnTrue_WhenPendingOrInProgress() {
        KitchenQueue pending = KitchenQueue.builder().status(DishStatus.PENDING).build();
        KitchenQueue inProgress = KitchenQueue.builder().status(DishStatus.IN_PROGRESS).build();
        KitchenQueue ready = KitchenQueue.builder().status(DishStatus.READY).build();
        assertTrue(pending.isActive());
        assertTrue(inProgress.isActive());
        assertFalse(ready.isActive());
    }

    @Test
    void isReady_ShouldReturnTrue_WhenReady() {
        KitchenQueue ready = KitchenQueue.builder().status(DishStatus.READY).build();
        KitchenQueue pending = KitchenQueue.builder().status(DishStatus.PENDING).build();
        assertTrue(ready.isReady());
        assertFalse(pending.isReady());
    }

    @Test
    void withStatus_ShouldReturnNewInstance() {
        KitchenQueue item = KitchenQueue.builder()
                .id(1L).orderId(100L).dishName("Test")
                .status(DishStatus.PENDING).build();
        KitchenQueue updated = item.withStatus(DishStatus.IN_PROGRESS);
        assertEquals(DishStatus.IN_PROGRESS, updated.getStatus());
        assertNotNull(updated.getStartedAt());
    }

    @Test
    void withStatus_ShouldSetCompletedAt_WhenReady() {
        KitchenQueue item = KitchenQueue.builder()
                .id(1L).status(DishStatus.IN_PROGRESS).build();
        KitchenQueue ready = item.withStatus(DishStatus.READY);
        assertNotNull(ready.getCompletedAt());
    }
}
