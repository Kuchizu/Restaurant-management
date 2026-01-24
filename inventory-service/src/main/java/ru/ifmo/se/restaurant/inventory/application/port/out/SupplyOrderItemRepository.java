package ru.ifmo.se.restaurant.inventory.application.port.out;

import ru.ifmo.se.restaurant.inventory.domain.entity.SupplyOrderItem;

import java.util.List;

public interface SupplyOrderItemRepository {
    SupplyOrderItem save(SupplyOrderItem item);
    List<SupplyOrderItem> findBySupplyOrderId(Long supplyOrderId);
}
