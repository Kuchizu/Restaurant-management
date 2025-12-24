package ru.ifmo.se.restaurant.order.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.ifmo.se.restaurant.order.dto.ErrorResponse;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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

    @Test
    void handleCircuitBreakerOpen_ReturnsServiceUnavailable() {
        CallNotPermittedException exception = mock(CallNotPermittedException.class);
        when(exception.getMessage()).thenReturn("CircuitBreaker is OPEN");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleCircuitBreakerOpen(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().getStatus()).isEqualTo(503);
                })
                .verifyComplete();
    }

    @Test
    void handleWebExchangeBindException_ReturnsBadRequest() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        WebExchangeBindException exception = mock(WebExchangeBindException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(exception.getMessage()).thenReturn("Validation failed");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleWebExchangeBindException(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().getDetails()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_UniqueEmail_ReturnsConflict() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("unique constraint violated: email");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleDataIntegrityViolation(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(entity.getBody().getMessage()).contains("email");
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_UniqueTableNumber_ReturnsConflict() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("unique constraint violated: table_number");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleDataIntegrityViolation(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(entity.getBody().getMessage()).contains("table");
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_ForeignKey_ReturnsConflict() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("foreign key constraint violated");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleDataIntegrityViolation(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(entity.getBody().getMessage()).contains("Referenced");
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_NotNull_ReturnsConflict() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("not null constraint violated");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleDataIntegrityViolation(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(entity.getBody().getMessage()).contains("Required");
                })
                .verifyComplete();
    }

    @Test
    void handleServerWebInputException_JsonDecoding_ReturnsBadRequest() {
        Exception cause = new Exception("JSON decoding error");
        ServerWebInputException exception = new ServerWebInputException("Invalid input", null, cause);

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleServerWebInputException(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(entity.getBody().getMessage()).contains("Malformed JSON");
                })
                .verifyComplete();
    }

    @Test
    void handleServerWebInputException_CannotDeserialize_ReturnsBadRequest() {
        Exception cause = new Exception("Cannot deserialize value");
        ServerWebInputException exception = new ServerWebInputException("Invalid input", null, cause);

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleServerWebInputException(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(entity.getBody().getMessage()).contains("Invalid data format");
                })
                .verifyComplete();
    }

    @Test
    void handleServerWebInputException_UnexpectedCharacter_ReturnsBadRequest() {
        Exception cause = new Exception("Unexpected character in input");
        ServerWebInputException exception = new ServerWebInputException("Invalid input", null, cause);

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleServerWebInputException(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(entity.getBody().getMessage()).contains("Syntax error");
                })
                .verifyComplete();
    }

    @Test
    void handleServerWebInputException_NoCause_ReturnsBadRequest() {
        ServerWebInputException exception = new ServerWebInputException("Invalid input");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleServerWebInputException(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                })
                .verifyComplete();
    }

    @Test
    void handleNoResourceFound_ActuatorPath_ReturnsError() {
        when(requestPath.value()).thenReturn("/actuator/health");
        NoResourceFoundException exception = new NoResourceFoundException(null);

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleNoResourceFound(exception, exchange);

        StepVerifier.create(response)
                .expectError(NoResourceFoundException.class)
                .verify();
    }

    @Test
    void handleWebClientRequestException_MenuService_ReturnsServiceUnavailable() {
        WebClientRequestException exception = mock(WebClientRequestException.class);
        when(exception.getUri()).thenReturn(URI.create("http://menu-service/api/dishes"));
        when(exception.getMessage()).thenReturn("Connection refused");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleWebClientRequestException(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(entity.getBody().getMessage()).contains("Menu");
                })
                .verifyComplete();
    }

    @Test
    void handleWebClientRequestException_KitchenService_ReturnsServiceUnavailable() {
        WebClientRequestException exception = mock(WebClientRequestException.class);
        when(exception.getUri()).thenReturn(URI.create("http://kitchen-service/api/queue"));
        when(exception.getMessage()).thenReturn("Connection refused");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleWebClientRequestException(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(entity.getBody().getMessage()).contains("Kitchen");
                })
                .verifyComplete();
    }

    @Test
    void handleWebClientRequestException_UnknownService_ReturnsServiceUnavailable() {
        WebClientRequestException exception = mock(WebClientRequestException.class);
        when(exception.getUri()).thenReturn(URI.create("http://unknown-service/api"));
        when(exception.getMessage()).thenReturn("Connection refused");

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleWebClientRequestException(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                })
                .verifyComplete();
    }

    @Test
    void handleWebClientResponseException_ReturnsDownstreamError() {
        WebClientResponseException exception = WebClientResponseException.create(
                500, "Internal Server Error", null, "error body".getBytes(), null);

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleWebClientResponseException(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(entity.getBody().getMessage()).contains("Downstream");
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_WithCause_ReturnsInternalServerError() {
        Exception cause = new Exception("Root cause");
        Exception exception = new Exception("Wrapper exception", cause);

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleGenericException(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(entity.getBody().getDetails()).containsKey("cause");
                })
                .verifyComplete();
    }

    @Test
    void handleDataIntegrityViolation_WithRootCause_ReturnsConflict() {
        Exception rootCause = new Exception("unique constraint: email violated");
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Data integrity", rootCause);

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleDataIntegrityViolation(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                })
                .verifyComplete();
    }

    @Test
    void handleValidation_WithNullRejectedValue_ReturnsUnprocessableEntity() {
        ValidationException exception = new ValidationException("Field is required", "name", null);

        Mono<ResponseEntity<ErrorResponse>> response = exceptionHandler.handleValidation(exception, exchange);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(entity.getBody().getDetails()).isNotNull();
                })
                .verifyComplete();
    }
}
