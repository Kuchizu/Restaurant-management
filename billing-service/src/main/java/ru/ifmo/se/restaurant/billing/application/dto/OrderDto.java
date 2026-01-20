package ru.ifmo.se.restaurant.billing.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Table ID is required")
    private Long tableId;

    @NotNull(message = "Waiter ID is required")
    private Long waiterId;

    private String status;

    @DecimalMin(value = "0.0", message = "Total amount must be non-negative")
    private BigDecimal totalAmount;

    private String specialRequests;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private List<OrderItemDto> items;
}
