package ru.ifmo.se.restaurant.order.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("orders")
public class Order {
    @Id
    private Long id;
    private Long tableId;
    private Long waiterId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String specialRequests;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
}
