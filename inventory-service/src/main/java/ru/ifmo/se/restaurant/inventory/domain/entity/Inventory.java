package ru.ifmo.se.restaurant.inventory.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Inventory {
    private final Long id;
    private final Ingredient ingredient;
    private final BigDecimal quantity;
    private final BigDecimal minQuantity;
    private final BigDecimal maxQuantity;
    private final LocalDateTime lastUpdated;

    public boolean isLowStock() {
        return quantity.compareTo(minQuantity) < 0;
    }

    public boolean canAdjust(BigDecimal adjustment) {
        BigDecimal newQuantity = quantity.add(adjustment);
        return newQuantity.compareTo(BigDecimal.ZERO) >= 0;
    }

    public Inventory adjustQuantity(BigDecimal adjustment) {
        if (!canAdjust(adjustment)) {
            throw new IllegalArgumentException("Adjustment would result in negative inventory");
        }
        return new Inventory(
            id,
            ingredient,
            quantity.add(adjustment),
            minQuantity,
            maxQuantity,
            LocalDateTime.now()
        );
    }

    public Inventory updateQuantity(BigDecimal newQuantity) {
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        return new Inventory(
            id,
            ingredient,
            newQuantity,
            minQuantity,
            maxQuantity,
            LocalDateTime.now()
        );
    }

    public Inventory updateThresholds(BigDecimal newMinQuantity, BigDecimal newMaxQuantity) {
        return new Inventory(
            id,
            ingredient,
            quantity,
            newMinQuantity != null ? newMinQuantity : minQuantity,
            newMaxQuantity != null ? newMaxQuantity : maxQuantity,
            LocalDateTime.now()
        );
    }
}
