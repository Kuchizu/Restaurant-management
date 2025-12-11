package ru.ifmo.se.restaurant.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;

    @NotNull(message = "Table ID cannot be null")
    private Long tableId;

    @NotNull(message = "Waiter ID cannot be null")
    private Long waiterId;

    private OrderStatus status;
    private BigDecimal totalAmount;
    private String specialRequests;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private List<OrderItemDto> items;
}
