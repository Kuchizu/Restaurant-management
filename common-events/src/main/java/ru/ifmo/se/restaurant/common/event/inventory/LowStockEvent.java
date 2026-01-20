package ru.ifmo.se.restaurant.common.event.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockEvent {
    private Long inventoryId;
    private Long ingredientId;
    private String ingredientName;
    private BigDecimal currentQuantity;
    private BigDecimal minimumQuantity;
    private String unit;
    private Instant detectedAt;
}
