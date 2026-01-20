package ru.ifmo.se.restaurant.common.event.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSentToKitchenEvent {
    private Long orderId;
    private Long tableId;
    private Instant sentAt;
    private List<KitchenItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KitchenItem {
        private Long orderItemId;
        private Long dishId;
        private String dishName;
        private Integer quantity;
        private String specialInstructions;
    }
}
