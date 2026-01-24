package ru.ifmo.se.restaurant.inventory.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import ru.ifmo.se.restaurant.inventory.domain.valueobject.SupplyOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SupplyOrder {
    private final Long id;
    private final Supplier supplier;
    private final LocalDateTime orderDate;
    private final LocalDateTime deliveryDate;
    private final SupplyOrderStatus status;
    private final BigDecimal totalCost;
    private final String notes;
    private final List<SupplyOrderItem> items;

    public SupplyOrder updateStatus(SupplyOrderStatus newStatus) {
        LocalDateTime newDeliveryDate = deliveryDate;
        if (newStatus == SupplyOrderStatus.DELIVERED && deliveryDate == null) {
            newDeliveryDate = LocalDateTime.now();
        }
        return new SupplyOrder(
            id,
            supplier,
            orderDate,
            newDeliveryDate,
            newStatus,
            totalCost,
            notes,
            items
        );
    }

    public boolean isDelivered() {
        return status == SupplyOrderStatus.DELIVERED;
    }

    public boolean canBeDelivered() {
        return status == SupplyOrderStatus.SHIPPED || status == SupplyOrderStatus.CONFIRMED;
    }
}
