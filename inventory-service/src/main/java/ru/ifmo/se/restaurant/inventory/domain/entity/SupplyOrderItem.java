package ru.ifmo.se.restaurant.inventory.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class SupplyOrderItem {
    private final Long id;
    private final Ingredient ingredient;
    private final BigDecimal quantity;
    private final BigDecimal unitPrice;

    public BigDecimal getTotalPrice() {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(unitPrice);
    }
}
