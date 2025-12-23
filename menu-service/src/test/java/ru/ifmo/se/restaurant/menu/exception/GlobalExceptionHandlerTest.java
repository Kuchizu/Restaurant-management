package ru.ifmo.se.restaurant.menu.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.codec.DecodingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import ru.ifmo.se.restaurant.menu.dto.ErrorResponse;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private RequestPath path;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(exchange.getRequest()).thenReturn(request);
        when(request.getPath()).thenReturn(path);
        when(path.value()).thenReturn("/api/test");
    }

    @Test
    void handleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, exchange).block();
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleBusinessConflict() {
        BusinessConflictException ex = new BusinessConflictException("Conflict", "Dish", 1L, "duplicate");
        ResponseEntity<ErrorResponse> response = handler.handleBusinessConflict(ex, exchange).block();
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleBusinessConflictWithoutDetails() {
        BusinessConflictException ex = new BusinessConflictException("Conflict");
        ResponseEntity<ErrorResponse> response = handler.handleBusinessConflict(ex, exchange).block();
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleServiceUnavailable() {
        ServiceUnavailableException ex = new ServiceUnavailableException("Service down", "kitchen-service", "getQueue");
        ResponseEntity<ErrorResponse> response = handler.handleServiceUnavailable(ex, exchange).block();
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void handleValidation() {
        ValidationException ex = new ValidationException("Invalid", "field", "value");
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, exchange).block();
        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    void handleValidationWithNullValue() {
        ValidationException ex = new ValidationException("Invalid", "field", null);
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, exchange).block();
        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    void handleValidationWithoutField() {
        ValidationException ex = new ValidationException("Invalid");
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, exchange).block();
        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    void handleBadRequest() {
        BadRequestException ex = new BadRequestException("Bad request");
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, exchange).block();
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleWebExchangeBindException() {
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(ex.getMessage()).thenReturn("Validation failed");
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleWebExchangeBindException(ex, exchange).block();
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleDataIntegrityViolation() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Constraint violation");
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, exchange).block();
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleDecodingException() {
        DecodingException ex = new DecodingException("Malformed JSON");
        ResponseEntity<ErrorResponse> response = handler.handleDecodingException(ex, exchange).block();
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleGenericException() {
        Exception ex = new RuntimeException("Unexpected error");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, exchange).block();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleGenericExceptionActuator() {
        when(path.value()).thenReturn("/actuator/health");
        Exception ex = new RuntimeException("Actuator error");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, exchange).block();
        assertNull(response);
    }
}
