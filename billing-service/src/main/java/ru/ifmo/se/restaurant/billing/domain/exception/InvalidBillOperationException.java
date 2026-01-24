package ru.ifmo.se.restaurant.billing.domain.exception;

public class InvalidBillOperationException extends RuntimeException {
    public InvalidBillOperationException(String message) {
        super(message);
    }
}
