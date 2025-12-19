package ru.ifmo.se.restaurant.order.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.ifmo.se.restaurant.order.dto.ErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private org.springframework.http.server.RequestPath requestPath;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
        when(exchange.getRequest()).thenReturn(request);
        when(request.getPath()).thenReturn(requestPath);
        when(requestPath.value()).thenReturn("/test/path");
    }

    @Test
    void handleResourceNotFoundException_ReturnsNotFound() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleResourceNotFound(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().getStatus()).isEqualTo(404);
                    assertThat(entity.getBody().getMessage()).isEqualTo("Resource not found");
                })
                .verifyComplete();
    }

    @Test
    void handleBusinessConflict_ReturnsConflict() {
        BusinessConflictException exception = new BusinessConflictException("Business conflict occurred");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleBusinessConflict(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().getStatus()).isEqualTo(409);
                })
                .verifyComplete();
    }

    @Test
    void handleServiceUnavailable_ReturnsServiceUnavailable() {
        ServiceUnavailableException exception = new ServiceUnavailableException(
                "Service unavailable", "menu-service", "getMenu");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleServiceUnavailable(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().getStatus()).isEqualTo(503);
                })
                .verifyComplete();
    }

    @Test
    void handleValidation_ReturnsUnprocessableEntity() {
        ValidationException exception = new ValidationException("Invalid field value", "testField", "invalidValue");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleValidation(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().getStatus()).isEqualTo(422);
                })
                .verifyComplete();
    }

    @Test
    void handleBadRequest_ReturnsBadRequest() {
        BadRequestException exception = new BadRequestException("Bad request");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleBadRequest(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().getStatus()).isEqualTo(400);
                    assertThat(entity.getBody().getMessage()).isEqualTo("Bad request");
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_ReturnsConflict() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Constraint violation");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleDataIntegrityViolation(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().getStatus()).isEqualTo(409);
                })
                .verifyComplete();
    }

    @Test
    void handleHttpMessageNotReadable_ReturnsBadRequest() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Malformed JSON", (org.springframework.http.HttpInputMessage) null);

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleHttpMessageNotReadable(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().getStatus()).isEqualTo(400);
                })
                .verifyComplete();
    }

    @Test
    void handleNoResourceFound_ReturnsNotFound() {
        NoResourceFoundException exception = new NoResourceFoundException(null);

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleNoResourceFound(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().getStatus()).isEqualTo(404);
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_ReturnsInternalServerError() {
        Exception exception = new Exception("Unexpected error");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleGenericException(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().getStatus()).isEqualTo(500);
                })
                .verifyComplete();
    }

    @Test
    void handleBusinessConflict_WithDetails_ReturnsConflictWithDetails() {
        BusinessConflictException exception = new BusinessConflictException(
                "Resource conflict",
                "Order",
                123L,
                "Order already exists"
        );

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleBusinessConflict(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().getDetails()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void handleValidation_WithoutField_ReturnsUnprocessableEntity() {
        ValidationException exception = new ValidationException("Validation failed");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleValidation(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(entity.getBody()).isNotNull();
                })
                .verifyComplete();
    }
}
