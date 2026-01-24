package ru.ifmo.se.restaurant.inventory.domain.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final String field;
    private final Object rejectedValue;

    public ValidationException(String message, String field, Object rejectedValue) {
        super(message);
        this.field = field;
        this.rejectedValue = rejectedValue;
    }

    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.rejectedValue = null;
    }
}
