package ru.ifmo.se.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SupplyOrderIngredient data transfer object")
public class SupplyOrderIngredientDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Supply order ingredient unique identifier", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotNull(message = "Ingredient ID cannot be null")
    @Schema(description = "Ingredient identifier", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long ingredientId;

    @Schema(description = "Ingredient name", example = "Tomatoes")
    private String ingredientName;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Quantity ordered", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer quantity;

    @NotNull(message = "Price per unit cannot be null")
    @Positive(message = "Price per unit must be positive")
    @Schema(description = "Price per unit", requiredMode = Schema.RequiredMode.REQUIRED, example = "2.50")
    private BigDecimal pricePerUnit;
}
