package ru.ifmo.se.restaurant.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private BigDecimal minQuantity;
    private BigDecimal maxQuantity;
    private LocalDateTime lastUpdated;
}
