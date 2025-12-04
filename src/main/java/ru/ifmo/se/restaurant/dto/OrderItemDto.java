package ru.ifmo.se.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OrderItem data transfer object")
public class OrderItemDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Order item unique identifier", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotNull(message = "Dish ID cannot be null")
    @Schema(description = "Dish identifier", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long dishId;

    @Schema(description = "Dish name", example = "Margherita Pizza")
    private String dishName;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Quantity of items ordered", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer quantity;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    @Schema(description = "Price per item", requiredMode = Schema.RequiredMode.REQUIRED, example = "12.99")
    private BigDecimal price;

    @Size(max = 500, message = "Special request cannot exceed 500 characters")
    @Schema(description = "Special preparation request", example = "No onions, extra cheese")
    private String specialRequest;
}
