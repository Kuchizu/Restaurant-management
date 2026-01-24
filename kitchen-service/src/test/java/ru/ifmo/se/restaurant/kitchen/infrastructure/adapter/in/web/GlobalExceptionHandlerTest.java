package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.in.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import ru.ifmo.se.restaurant.kitchen.domain.exception.DishNotFoundException;
import ru.ifmo.se.restaurant.kitchen.domain.exception.KitchenQueueNotFoundException;
import ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.in.web.dto.ErrorResponse;
import ru.ifmo.se.restaurant.kitchen.infrastructure.exception.BadRequestException;
import ru.ifmo.se.restaurant.kitchen.infrastructure.exception.BusinessConflictException;
import ru.ifmo.se.restaurant.kitchen.infrastructure.exception.ServiceUnavailableException;
import ru.ifmo.se.restaurant.kitchen.infrastructure.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/kitchen/queue/1");
    }

    @Test
    void handleResourceNotFound_KitchenQueueNotFoundException_ShouldReturn404() {
        KitchenQueueNotFoundException ex = new KitchenQueueNotFoundException("Queue item not found");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Queue item not found", response.getBody().getMessage());
    }

    @Test
    void handleResourceNotFound_DishNotFoundException_ShouldReturn404() {
        DishNotFoundException ex = new DishNotFoundException("Dish not found");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void handleBusinessConflict_WithDetails_ShouldReturn409() {
        BusinessConflictException ex = new BusinessConflictException(
                "Conflict occurred", "KitchenQueue", 1L, "Invalid state transition"
        );

        ResponseEntity<ErrorResponse> response = handler.handleBusinessConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertNotNull(response.getBody().getDetails());
    }

    @Test
    void handleBusinessConflict_WithoutDetails_ShouldReturn409() {
        BusinessConflictException ex = new BusinessConflictException("Simple conflict");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getDetails());
    }

    @Test
    void handleServiceUnavailable_ShouldReturn503() {
        ServiceUnavailableException ex = new ServiceUnavailableException(
                "Service unavailable", "menu-service", "getDish"
        );

        ResponseEntity<ErrorResponse> response = handler.handleServiceUnavailable(ex, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().getStatus());
        assertEquals("Service Unavailable", response.getBody().getError());
        assertNotNull(response.getBody().getDetails());
    }

    @Test
    void handleValidation_WithField_ShouldReturn422() {
        ValidationException ex = new ValidationException("Invalid value", "quantity", -1);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(422, response.getBody().getStatus());
        assertEquals("Validation Error", response.getBody().getError());
        assertNotNull(response.getBody().getDetails());
    }

    @Test
    void handleValidation_WithoutField_ShouldReturn422() {
        ValidationException ex = new ValidationException("General validation error", null, null);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getDetails());
    }

    @Test
    void handleBadRequest_ShouldReturn400() {
        BadRequestException ex = new BadRequestException("Invalid request");

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
    }
}
