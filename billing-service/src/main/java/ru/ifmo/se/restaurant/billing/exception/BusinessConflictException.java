package ru.ifmo.se.restaurant.billing.exception;

import lombok.Getter;

@Getter
public class BusinessConflictException extends RuntimeException {
    private final String resourceType;
    private final Object resourceId;
    private final String conflictReason;

    public BusinessConflictException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
        this.conflictReason = null;
    }

    public BusinessConflictException(String message, String resourceType, Object resourceId, String conflictReason) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.conflictReason = conflictReason;
    }
}
