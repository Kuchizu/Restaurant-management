package ru.ifmo.se.restaurant.billing.domain.exception;

public class BillAlreadyExistsException extends RuntimeException {
    public BillAlreadyExistsException(Long orderId) {
        super("Bill already exists for order: " + orderId);
    }
}
