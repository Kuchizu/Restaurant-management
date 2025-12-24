package ru.ifmo.se.restaurant.order.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.dto.ErrorResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResourceNotFound(
            ResourceNotFoundException ex,
            ServerWebExchange exchange) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(BusinessConflictException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBusinessConflict(
            BusinessConflictException ex,
            ServerWebExchange exchange) {
        log.warn("Business conflict: {}", ex.getMessage());
        Map<String, Object> details = new HashMap<>();
        if (ex.getResourceType() != null) {
            details.put("resourceType", ex.getResourceType());
            details.put("resourceId", ex.getResourceId());
            details.put("reason", ex.getConflictReason());
        }
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .details(details.isEmpty() ? null : details)
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(error));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleServiceUnavailable(
            ServiceUnavailableException ex,
            ServerWebExchange exchange) {
        log.error("Service unavailable: {}", ex.getMessage());
        Map<String, Object> details = Map.of(
                "serviceName", ex.getServiceName(),
                "operation", ex.getOperation()
        );
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Service Unavailable")
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .details(details)
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error));
    }

    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(
            ValidationException ex,
            ServerWebExchange exchange) {
        log.warn("Validation error: {}", ex.getMessage());
        Map<String, Object> details = null;
        if (ex.getField() != null) {
            details = Map.of(
                    "field", ex.getField(),
                    "rejectedValue", ex.getRejectedValue() != null ? ex.getRejectedValue() : "null"
            );
        }
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("Validation Error")
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .details(details)
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error));
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBadRequest(
            BadRequestException ex,
            ServerWebExchange exchange) {
        log.warn("Bad request: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebExchangeBindException(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {
        log.warn("Validation failed: {}", ex.getMessage());
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request validation failed")
                .path(exchange.getRequest().getPath().value())
                .details(Map.of("errors", fieldErrors))
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            ServerWebExchange exchange) {
        log.error("Data integrity violation: {}", ex.getMessage());

        String message = "Database constraint violation";
        Map<String, Object> details = new HashMap<>();

        String exceptionMessage = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        Throwable rootCause = ex.getRootCause();
        String rootCauseMessage = rootCause != null ? rootCause.getMessage() : "";

        if (exceptionMessage.contains("unique") || rootCauseMessage.contains("unique")) {
            if (exceptionMessage.contains("email") || rootCauseMessage.toLowerCase().contains("email")) {
                message = "An employee with this email already exists";
                details.put("field", "email");
                details.put("constraint", "unique");
            } else if (exceptionMessage.contains("table_number") || rootCauseMessage.toLowerCase().contains("table_number")) {
                message = "A table with this number already exists";
                details.put("field", "tableNumber");
                details.put("constraint", "unique");
            } else {
                message = "A record with this value already exists";
                details.put("constraint", "unique");
            }
        } else if (exceptionMessage.contains("foreign key") || rootCauseMessage.toLowerCase().contains("foreign key")) {
            message = "Referenced record does not exist";
            details.put("constraint", "foreign_key");
        } else if (exceptionMessage.contains("not null") || rootCauseMessage.toLowerCase().contains("not null")) {
            message = "Required field is missing";
            details.put("constraint", "not_null");
        }

        if (rootCauseMessage != null && !rootCauseMessage.isEmpty()) {
            details.put("technicalDetails", rootCauseMessage);
        }

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .details(details.isEmpty() ? null : details)
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(error));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            ServerWebExchange exchange) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Malformed JSON request")
                .path(exchange.getRequest().getPath().value())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleServerWebInputException(
            ServerWebInputException ex,
            ServerWebExchange exchange) {
        log.warn("Invalid request input: {}", ex.getMessage());

        String message = "Invalid request format";
        Map<String, Object> details = new HashMap<>();

        Throwable cause = ex.getCause();
        if (cause != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage != null) {
                if (causeMessage.contains("JSON decoding error") || causeMessage.contains("Unrecognized token")) {
                    message = "Malformed JSON: Please check your request syntax";
                    details.put("hint", "Common issues: missing quotes, extra characters, typos in field names");
                } else if (causeMessage.contains("Cannot deserialize")) {
                    message = "Invalid data format in request body";
                } else if (causeMessage.contains("Unexpected character")) {
                    message = "Syntax error in JSON request";
                }
                details.put("error", causeMessage);
            }
        }

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .details(details.isEmpty() ? null : details)
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNoResourceFound(
            NoResourceFoundException ex, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();

        // Don't handle actuator endpoints - let Spring Boot Actuator handle them
        if (path.startsWith("/actuator")) {
            return Mono.error(ex);
        }

        // Don't log - these are benign 404s for missing static resources (favicon.ico, etc.)
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("Resource not found")
                .path(path)
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(WebClientRequestException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebClientRequestException(
            WebClientRequestException ex,
            ServerWebExchange exchange) {
        log.error("WebClient connection error: {}", ex.getMessage());

        String serviceName = "downstream service";
        String uri = ex.getUri() != null ? ex.getUri().toString() : "unknown";

        if (uri.contains("menu-service")) {
            serviceName = "Menu service";
        } else if (uri.contains("kitchen-service")) {
            serviceName = "Kitchen service";
        }

        Map<String, Object> details = new HashMap<>();
        details.put("serviceName", serviceName);
        details.put("uri", uri);
        details.put("reason", "Connection refused or service unavailable");

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Service Unavailable")
                .message(serviceName + " is currently unavailable")
                .path(exchange.getRequest().getPath().value())
                .details(details)
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebClientResponseException(
            WebClientResponseException ex,
            ServerWebExchange exchange) {
        log.error("WebClient response error: {} - {}", ex.getStatusCode(), ex.getMessage());

        Map<String, Object> details = new HashMap<>();
        details.put("statusCode", ex.getStatusCode().value());
        details.put("responseBody", ex.getResponseBodyAsString());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatusCode().value())
                .error(ex.getStatusCode().toString())
                .message("Downstream service returned an error")
                .path(exchange.getRequest().getPath().value())
                .details(details)
                .build();
        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex,
            ServerWebExchange exchange) {
        log.error("Unexpected error occurred: {} - {}", ex.getClass().getName(), ex.getMessage(), ex);
        
        // Provide more helpful error message for common issues
        String message = "An unexpected error occurred. Please contact support.";
        Map<String, Object> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getSimpleName());
        
        if (ex.getCause() != null) {
            details.put("cause", ex.getCause().getMessage());
        }
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .details(details)
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}
