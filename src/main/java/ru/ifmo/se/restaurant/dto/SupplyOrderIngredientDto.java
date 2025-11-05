package ru.ifmo.se.restaurant.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrderIngredientDto {
    private Long id;

    @NotNull(message = "Ingredient ID cannot be null")
    private Long ingredientId;

    private String ingredientName;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Price per unit cannot be null")
    @Positive(message = "Price per unit must be positive")
    private BigDecimal pricePerUnit;
}

