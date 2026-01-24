package ru.ifmo.se.restaurant.kitchen.domain.exception;

import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(DishStatus currentStatus, DishStatus newStatus) {
        super(String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
    }
}
