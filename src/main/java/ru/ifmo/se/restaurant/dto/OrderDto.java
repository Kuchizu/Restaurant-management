package ru.ifmo.se.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Order data transfer object")
public class OrderDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Order unique identifier", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotNull(message = "Table ID cannot be null")
    @Schema(description = "Table ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long tableId;

    @Schema(description = "Table number", accessMode = Schema.AccessMode.READ_ONLY, example = "5")
    private Integer tableNumber;

    @NotNull(message = "Waiter ID cannot be null")
    @Schema(description = "Waiter ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long waiterId;

    @Schema(description = "Waiter name", accessMode = Schema.AccessMode.READ_ONLY, example = "John Doe")
    private String waiterName;

    @Schema(description = "Order status", accessMode = Schema.AccessMode.READ_ONLY, example = "CREATED")
    private OrderStatus status;

    @Schema(description = "Total order amount", accessMode = Schema.AccessMode.READ_ONLY, example = "45.99")
    private BigDecimal totalAmount;

    @Size(max = 500, message = "Special requests cannot exceed 500 characters")
    @Schema(description = "Special requests", example = "No onions please")
    private String specialRequests;

    @Schema(description = "Order creation timestamp", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-12-05T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Order closed timestamp", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-12-05T11:30:00")
    private LocalDateTime closedAt;

    @Schema(description = "List of order items")
    private List<OrderItemDto> items;
}
