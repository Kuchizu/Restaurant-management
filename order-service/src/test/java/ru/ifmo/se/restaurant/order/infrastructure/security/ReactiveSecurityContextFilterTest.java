package ru.ifmo.se.restaurant.order.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReactiveSecurityContextFilterTest {

    private ReactiveSecurityContextFilter filter;
    private WebFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new ReactiveSecurityContextFilter();
        filterChain = mock(WebFilterChain.class);
        when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_ShouldAllowPublicPath_Actuator() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_ShouldAllowPublicPath_Swagger() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/swagger-ui/index.html")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_ShouldAllowInternalGetRequest_WithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_ShouldReturnUnauthorized_WhenNoHeaders() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/orders")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_ShouldReturnForbidden_WhenInvalidRole() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/orders")
                .header("X-User-Id", "1")
                .header("X-User-Role", "CHEF")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_ShouldAllowWaiterRequest() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/orders")
                .header("X-User-Id", "1")
                .header("X-User-Role", "WAITER")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_ShouldAllowAdminRequest() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/orders")
                .header("X-User-Id", "1")
                .header("X-User-Role", "ADMIN")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_ShouldAllowManagerRequest() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/orders")
                .header("X-User-Id", "1")
                .header("X-User-Role", "MANAGER")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_ShouldSetEmployeeId_WhenProvided() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/orders")
                .header("X-User-Id", "1")
                .header("X-User-Role", "WAITER")
                .header("X-Employee-Id", "100")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void getCurrentUser_ShouldReturnEmpty_WhenNoContext() {
        StepVerifier.create(ReactiveSecurityContextFilter.getCurrentUser())
                .verifyComplete();
    }
}
