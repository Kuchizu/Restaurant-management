package ru.ifmo.se.restaurant.billing.domain.exception;

public class BillNotFoundException extends RuntimeException {
    public BillNotFoundException(Long id) {
        super("Bill not found with id: " + id);
    }

    public BillNotFoundException(String message) {
        super(message);
    }
}
