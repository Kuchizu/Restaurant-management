package ru.ifmo.se.restaurant.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KitchenQueueRequest {
    private Long orderId;
    private Long orderItemId;
    private String dishName;
    private Integer quantity;
    private String specialRequest;
}
