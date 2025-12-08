package ru.ifmo.se.restaurant.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long tableId;
    private Long waiterId;
    private String status;
    private BigDecimal totalAmount;
    private String specialRequests;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private List<OrderItemDto> items;
}
