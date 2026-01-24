package ru.ifmo.se.restaurant.order.infrastructure.adapter.in.web;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebInputException;
import reactor.test.StepVerifier;
import ru.ifmo.se.restaurant.order.application.dto.ErrorResponse;
import ru.ifmo.se.restaurant.order.domain.exception.*;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        exchange = MockServerWebExchange.from(request);
    }

    @Test
    void handleResourceNotFound_ShouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Order not found with id: 1");

        StepVerifier.create(handler.handleResourceNotFound(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(404, response.getBody().getStatus());
                })
                .verifyComplete();
    }

    @Test
    void handleBusinessConflict_ShouldReturn409_WithDetails() {
        BusinessConflictException ex = new BusinessConflictException("Order conflict", "Order", 1L, "Cannot cancel paid order");

        StepVerifier.create(handler.handleBusinessConflict(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertNotNull(response.getBody().getDetails());
                })
                .verifyComplete();
    }

    @Test
    void handleBusinessConflict_ShouldReturn409_WithoutDetails() {
        BusinessConflictException ex = new BusinessConflictException("Simple conflict");

        StepVerifier.create(handler.handleBusinessConflict(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                    assertNull(response.getBody().getDetails());
                })
                .verifyComplete();
    }

    @Test
    void handleServiceUnavailable_ShouldReturn503() {
        ServiceUnavailableException ex = new ServiceUnavailableException("Service unavailable", "kitchen-service", "sendOrder");

        StepVerifier.create(handler.handleServiceUnavailable(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(503, response.getBody().getStatus());
                })
                .verifyComplete();
    }

    @Test
    void handleValidation_ShouldReturn422_WithDetails() {
        ValidationException ex = new ValidationException("Invalid quantity", "quantity", 0);

        StepVerifier.create(handler.handleValidation(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
                    assertNotNull(response.getBody().getDetails());
                })
                .verifyComplete();
    }

    @Test
    void handleValidation_ShouldReturn422_WithNullRejectedValue() {
        ValidationException ex = new ValidationException("Invalid field", "field", null);

        StepVerifier.create(handler.handleValidation(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleBadRequest_ShouldReturn400() {
        BadRequestException ex = new BadRequestException("Bad request");

        StepVerifier.create(handler.handleBadRequest(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_UniqueEmail_ShouldReturn409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("unique constraint email violated");

        StepVerifier.create(handler.handleDataIntegrityViolation(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                    assertTrue(response.getBody().getMessage().contains("email"));
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_UniqueTableNumber_ShouldReturn409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("unique constraint table_number violated");

        StepVerifier.create(handler.handleDataIntegrityViolation(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                    assertTrue(response.getBody().getMessage().contains("table"));
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_ForeignKey_ShouldReturn409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("foreign key constraint violated");

        StepVerifier.create(handler.handleDataIntegrityViolation(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_NotNull_ShouldReturn409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("not null constraint violated");

        StepVerifier.create(handler.handleDataIntegrityViolation(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleHttpMessageNotReadable_ShouldReturn400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON");

        StepVerifier.create(handler.handleHttpMessageNotReadable(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals("Malformed JSON request", response.getBody().getMessage());
                })
                .verifyComplete();
    }

    @Test
    void handleServerWebInputException_ShouldReturn400() {
        ServerWebInputException ex = new ServerWebInputException("Invalid input");

        StepVerifier.create(handler.handleServerWebInputException(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleServerWebInputException_WithJsonDecodingCause_ShouldReturn400() {
        Exception cause = new RuntimeException("JSON decoding error");
        ServerWebInputException ex = new ServerWebInputException("Invalid input", null, cause);

        StepVerifier.create(handler.handleServerWebInputException(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertTrue(response.getBody().getMessage().contains("Malformed JSON"));
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        Exception ex = new RuntimeException("Unexpected error");

        StepVerifier.create(handler.handleGenericException(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_WithCause_ShouldReturn500() {
        Exception cause = new IllegalArgumentException("Root cause");
        Exception ex = new RuntimeException("Wrapper", cause);

        StepVerifier.create(handler.handleGenericException(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertNotNull(response.getBody().getDetails());
                })
                .verifyComplete();
    }
}
