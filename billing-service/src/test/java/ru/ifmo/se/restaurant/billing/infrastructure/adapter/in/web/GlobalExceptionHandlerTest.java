package ru.ifmo.se.restaurant.billing.infrastructure.adapter.in.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import ru.ifmo.se.restaurant.billing.application.dto.ErrorResponse;
import ru.ifmo.se.restaurant.billing.domain.exception.BillAlreadyExistsException;
import ru.ifmo.se.restaurant.billing.domain.exception.BillNotFoundException;
import ru.ifmo.se.restaurant.billing.domain.exception.InvalidBillOperationException;
import ru.ifmo.se.restaurant.billing.domain.exception.OrderServiceException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/bills/1");
    }

    @Test
    void handleBillNotFoundException_ShouldReturn404() {
        BillNotFoundException ex = new BillNotFoundException("Bill not found");

        ResponseEntity<ErrorResponse> response = handler.handleBillNotFoundException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Bill not found", response.getBody().getMessage());
    }

    @Test
    void handleBillAlreadyExists_ShouldReturn409() {
        BillAlreadyExistsException ex = new BillAlreadyExistsException(100L);

        ResponseEntity<ErrorResponse> response = handler.handleBillAlreadyExists(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
    }

    @Test
    void handleInvalidBillOperation_ShouldReturn409() {
        InvalidBillOperationException ex = new InvalidBillOperationException("Cannot pay already paid bill");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidBillOperation(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
    }

    @Test
    void handleOrderServiceException_ShouldReturn503() {
        OrderServiceException ex = new OrderServiceException("Order service unavailable");

        ResponseEntity<ErrorResponse> response = handler.handleOrderServiceException(ex, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().getStatus());
        assertEquals("Service Unavailable", response.getBody().getError());
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturn409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Unique constraint violation");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Database constraint violation", response.getBody().getMessage());
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
