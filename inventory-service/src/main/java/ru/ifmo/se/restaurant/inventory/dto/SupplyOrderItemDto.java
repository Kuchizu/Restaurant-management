package ru.ifmo.se.restaurant.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrderItemDto {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
}
