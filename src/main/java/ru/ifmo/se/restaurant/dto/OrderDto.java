package ru.ifmo.se.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull(message = "Table ID cannot be null")
    private Long tableId;

    private Integer tableNumber;

    @NotNull(message = "Waiter ID cannot be null")
    private Long waiterId;

    private String waiterName;

    private OrderStatus status;

    private BigDecimal totalAmount;

    @Size(max = 500, message = "Special requests cannot exceed 500 characters")
    private String specialRequests;

    private LocalDateTime createdAt;

    private LocalDateTime closedAt;

    private List<OrderItemDto> items;
}
