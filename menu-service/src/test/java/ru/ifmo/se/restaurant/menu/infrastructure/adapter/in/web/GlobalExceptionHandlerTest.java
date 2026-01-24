package ru.ifmo.se.restaurant.menu.infrastructure.adapter.in.web;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.codec.DecodingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.test.StepVerifier;
import ru.ifmo.se.restaurant.menu.application.dto.ErrorResponse;
import ru.ifmo.se.restaurant.menu.domain.exception.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/menu").build();
        exchange = MockServerWebExchange.from(request);
    }

    @Test
    void handleResourceNotFoundException_ShouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Dish not found with id: 1");

        StepVerifier.create(handler.handleResourceNotFoundException(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(404, response.getBody().getStatus());
                })
                .verifyComplete();
    }

    @Test
    void handleBusinessConflict_ShouldReturn409() {
        BusinessConflictException ex = new BusinessConflictException("Dish conflict", "Dish", 1L, "Already exists");

        StepVerifier.create(handler.handleBusinessConflict(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(409, response.getBody().getStatus());
                })
                .verifyComplete();
    }

    @Test
    void handleBusinessConflict_ShouldReturn409_WithoutDetails() {
        BusinessConflictException ex = new BusinessConflictException("Simple conflict");

        StepVerifier.create(handler.handleBusinessConflict(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleServiceUnavailable_ShouldReturn503() {
        ServiceUnavailableException ex = new ServiceUnavailableException("Service unavailable", "inventory-service", "checkStock");

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
        ValidationException ex = new ValidationException("Invalid price", "price", -10.0);

        StepVerifier.create(handler.handleValidation(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertNotNull(response.getBody().getDetails());
                })
                .verifyComplete();
    }

    @Test
    void handleValidation_ShouldReturn422_WithoutDetails() {
        ValidationException ex = new ValidationException("Validation failed");

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
                    assertNotNull(response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void handleServerWebInput_ShouldReturn400() {
        ServerWebInputException ex = new ServerWebInputException("Invalid input");

        StepVerifier.create(handler.handleServerWebInput(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturn409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Constraint violation");

        StepVerifier.create(handler.handleDataIntegrityViolation(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleDecodingException_ShouldReturn400() {
        DecodingException ex = new DecodingException("Invalid JSON");

        StepVerifier.create(handler.handleDecodingException(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals("Malformed JSON request", response.getBody().getMessage());
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        Exception ex = new RuntimeException("Unexpected");

        StepVerifier.create(handler.handleGenericException(ex, exchange))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_ShouldReturnEmpty_ForActuator() {
        MockServerHttpRequest actuatorRequest = MockServerHttpRequest.get("/actuator/health").build();
        MockServerWebExchange actuatorExchange = MockServerWebExchange.from(actuatorRequest);
        Exception ex = new RuntimeException("Unexpected");

        StepVerifier.create(handler.handleGenericException(ex, actuatorExchange))
                .verifyComplete();
    }
}
