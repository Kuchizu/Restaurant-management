package ru.ifmo.se.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.model.DishStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "KitchenQueue data transfer object")
public class KitchenQueueDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Kitchen queue item unique identifier", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotNull(message = "Order ID cannot be null")
    @Schema(description = "Order identifier", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long orderId;

    @NotNull(message = "Order item ID cannot be null")
    @Schema(description = "Order item identifier", requiredMode = Schema.RequiredMode.REQUIRED, example = "25")
    private Long orderItemId;

    @Schema(description = "Dish name", example = "Caesar Salad")
    private String dishName;

    @Schema(description = "Quantity to prepare", example = "2")
    private Integer quantity;

    @Schema(description = "Preparation status", example = "IN_PROGRESS")
    private DishStatus status;

    @Schema(description = "Special preparation request", example = "No croutons")
    private String specialRequest;

    @Schema(description = "Creation timestamp", example = "2025-12-05T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Preparation start timestamp", example = "2025-12-05T14:35:00")
    private LocalDateTime startedAt;

    @Schema(description = "Completion timestamp", example = "2025-12-05T14:50:00")
    private LocalDateTime completedAt;
}
