package ru.ifmo.se.restaurant.common.event.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private Long tableId;
    private Long waiterId;
    private Instant createdAt;
    private List<OrderItemData> items;
    private BigDecimal totalAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemData {
        private Long itemId;
        private Long dishId;
        private String dishName;
        private Integer quantity;
        private BigDecimal price;
    }
}
