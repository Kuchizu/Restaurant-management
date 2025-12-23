package ru.ifmo.se.restaurant.kitchen.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.ifmo.se.restaurant.kitchen.dto.ErrorResponse;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleBusinessConflict() {
        BusinessConflictException ex = new BusinessConflictException("Conflict", "Order", 1L, "duplicate");
        ResponseEntity<ErrorResponse> response = handler.handleBusinessConflict(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleBusinessConflictWithoutDetails() {
        BusinessConflictException ex = new BusinessConflictException("Conflict");
        ResponseEntity<ErrorResponse> response = handler.handleBusinessConflict(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleServiceUnavailable() {
        ServiceUnavailableException ex = new ServiceUnavailableException("Service down", "menu-service", "getMenu");
        ResponseEntity<ErrorResponse> response = handler.handleServiceUnavailable(ex, request);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void handleValidation() {
        ValidationException ex = new ValidationException("Invalid", "field", "value");
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    void handleValidationWithNullValue() {
        ValidationException ex = new ValidationException("Invalid", "field", null);
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    void handleValidationWithoutField() {
        ValidationException ex = new ValidationException("Invalid");
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    void handleBadRequest() {
        BadRequestException ex = new BadRequestException("Bad request");
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleMethodArgumentNotValid() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(ex.getMessage()).thenReturn("Validation failed");
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleHttpMessageNotReadable() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Malformed JSON");
        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleNoResourceFound() throws NoResourceFoundException {
        NoResourceFoundException ex = mock(NoResourceFoundException.class);
        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleNoResourceFoundActuator() {
        when(request.getRequestURI()).thenReturn("/actuator/health");
        NoResourceFoundException ex = mock(NoResourceFoundException.class);
        assertThrows(NoResourceFoundException.class, () -> handler.handleNoResourceFound(ex, request));
    }

    @Test
    void handleGenericException() {
        Exception ex = new RuntimeException("Unexpected error");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
