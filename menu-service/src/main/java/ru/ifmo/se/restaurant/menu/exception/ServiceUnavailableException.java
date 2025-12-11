package ru.ifmo.se.restaurant.menu.exception;

import lombok.Getter;

@Getter
public class ServiceUnavailableException extends RuntimeException {
    private final String serviceName;
    private final String operation;

    public ServiceUnavailableException(String message, String serviceName, String operation) {
        super(message);
        this.serviceName = serviceName;
        this.operation = operation;
    }
}
