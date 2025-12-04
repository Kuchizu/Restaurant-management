package ru.ifmo.se.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Inventory data transfer object")
public class InventoryDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Inventory unique identifier", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotNull(message = "Ingredient ID cannot be null")
    @Schema(description = "Ingredient identifier", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long ingredientId;

    @Schema(description = "Ingredient name", example = "Tomatoes")
    private String ingredientName;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Schema(description = "Available quantity", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer quantity;

    @NotNull(message = "Reserved quantity cannot be null")
    @Min(value = 0, message = "Reserved quantity cannot be negative")
    @Schema(description = "Reserved quantity for orders", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer reservedQuantity;

    @Schema(description = "Price per unit", example = "2.50")
    private BigDecimal pricePerUnit;

    @NotNull(message = "Expiry date cannot be null")
    @Schema(description = "Expiry date", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-12-31")
    private LocalDate expiryDate;

    @Schema(description = "Date received", example = "2025-12-01")
    private LocalDate receivedDate;
}
