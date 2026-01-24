package ru.ifmo.se.restaurant.kitchen.domain.exception;

public class KitchenQueueNotFoundException extends RuntimeException {
    public KitchenQueueNotFoundException(Long id) {
        super("Kitchen queue item not found with id: " + id);
    }

    public KitchenQueueNotFoundException(String message) {
        super(message);
    }
}
