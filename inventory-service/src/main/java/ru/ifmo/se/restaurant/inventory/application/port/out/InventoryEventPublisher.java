package ru.ifmo.se.restaurant.inventory.application.port.out;

import ru.ifmo.se.restaurant.inventory.entity.Inventory;

/**
 * Output port for publishing inventory domain events.
 * This is part of Clean Architecture - the application layer defines the port,
 * and the infrastructure layer provides the implementation.
 */
public interface InventoryEventPublisher {

    /**
     * Publishes a LOW_STOCK event when inventory falls below minimum threshold.
     *
     * @param inventory the inventory item with low stock
     */
    void publishLowStock(Inventory inventory);
}
