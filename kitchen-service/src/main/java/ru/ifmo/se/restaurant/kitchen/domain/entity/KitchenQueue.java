package ru.ifmo.se.restaurant.kitchen.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class KitchenQueue {
    private final Long id;
    private final Long orderId;
    private final Long orderItemId;
    private final String dishName;
    private final Integer quantity;
    private final DishStatus status;
    private final String specialRequest;
    private final LocalDateTime createdAt;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;

    public KitchenQueue withStatus(DishStatus newStatus) {
        return KitchenQueue.builder()
                .id(this.id)
                .orderId(this.orderId)
                .orderItemId(this.orderItemId)
                .dishName(this.dishName)
                .quantity(this.quantity)
                .status(newStatus)
                .specialRequest(this.specialRequest)
                .createdAt(this.createdAt)
                .startedAt(newStatus == DishStatus.IN_PROGRESS && this.startedAt == null ? LocalDateTime.now() : this.startedAt)
                .completedAt(newStatus == DishStatus.READY && this.completedAt == null ? LocalDateTime.now() : this.completedAt)
                .build();
    }

    public boolean isActive() {
        return status == DishStatus.PENDING || status == DishStatus.IN_PROGRESS;
    }

    public boolean isReady() {
        return status == DishStatus.READY;
    }
}
