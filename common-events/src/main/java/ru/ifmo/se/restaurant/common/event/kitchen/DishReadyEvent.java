package ru.ifmo.se.restaurant.common.event.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishReadyEvent {
    private Long kitchenQueueId;
    private Long orderId;
    private Long orderItemId;
    private String dishName;
    private Integer quantity;
    private Instant readyAt;
}
