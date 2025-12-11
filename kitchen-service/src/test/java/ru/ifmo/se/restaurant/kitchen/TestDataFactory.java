package ru.ifmo.se.restaurant.kitchen;

import ru.ifmo.se.restaurant.kitchen.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.entity.KitchenQueue;

import java.time.LocalDateTime;

public class TestDataFactory {

    public static KitchenQueue createMockKitchenQueue(Long id) {
        KitchenQueue queue = new KitchenQueue();
        queue.setId(id);
        queue.setOrderId(100L);
        queue.setOrderItemId(200L);
        queue.setDishName("Test Dish");
        queue.setQuantity(2);
        queue.setStatus(DishStatus.PENDING);
        queue.setSpecialRequest("No onions");
        queue.setCreatedAt(LocalDateTime.now());
        return queue;
    }

    public static KitchenQueue createKitchenQueueWithStatus(Long id, DishStatus status) {
        KitchenQueue queue = createMockKitchenQueue(id);
        queue.setStatus(status);

        if (status == DishStatus.IN_PROGRESS || status == DishStatus.READY || status == DishStatus.SERVED) {
            queue.setStartedAt(LocalDateTime.now().minusMinutes(10));
        }

        if (status == DishStatus.READY || status == DishStatus.SERVED) {
            queue.setCompletedAt(LocalDateTime.now().minusMinutes(5));
        }

        return queue;
    }

    public static KitchenQueueDto createMockKitchenQueueDto(Long id) {
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setId(id);
        dto.setOrderId(100L);
        dto.setOrderItemId(200L);
        dto.setDishName("Test Dish");
        dto.setQuantity(2);
        dto.setStatus(DishStatus.PENDING);
        dto.setSpecialRequest("No onions");
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }

    public static KitchenQueueDto createKitchenQueueDtoForCreation() {
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setOrderId(100L);
        dto.setOrderItemId(200L);
        dto.setDishName("New Dish");
        dto.setQuantity(1);
        dto.setSpecialRequest("Extra spicy");
        return dto;
    }

    public static KitchenQueue createKitchenQueueForOrder(Long id, Long orderId, String dishName) {
        KitchenQueue queue = new KitchenQueue();
        queue.setId(id);
        queue.setOrderId(orderId);
        queue.setOrderItemId(id * 10);
        queue.setDishName(dishName);
        queue.setQuantity(1);
        queue.setStatus(DishStatus.PENDING);
        queue.setCreatedAt(LocalDateTime.now());
        return queue;
    }
}
