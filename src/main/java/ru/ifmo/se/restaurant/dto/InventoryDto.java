package ru.ifmo.se.restaurant.dto;

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
public class InventoryDto {
    private Long id;

    @NotNull(message = "Ingredient ID cannot be null")
    private Long ingredientId;

    private String ingredientName;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "Reserved quantity cannot be null")
    @Min(value = 0, message = "Reserved quantity cannot be negative")
    private Integer reservedQuantity;

    private BigDecimal pricePerUnit;

    @NotNull(message = "Expiry date cannot be null")
    private LocalDate expiryDate;

    private LocalDate receivedDate;
}

