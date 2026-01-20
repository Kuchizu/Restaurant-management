package ru.ifmo.se.restaurant.order.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.order.domain.valueobject.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private Long tableId;
    private Long waiterId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String specialRequests;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private Long version;
}
