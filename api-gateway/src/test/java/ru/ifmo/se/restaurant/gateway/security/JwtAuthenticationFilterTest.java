package ru.ifmo.se.restaurant.gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.ifmo.se.restaurant.gateway.entity.UserRole;
import ru.ifmo.se.restaurant.gateway.service.JwtService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private WebFilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService);
    }

    @Test
    void filter_PublicLoginPath_ShouldContinueWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verifyNoInteractions(jwtService);
    }

    @Test
    void filter_PublicRefreshPath_ShouldContinueWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/refresh")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_PublicInitPath_ShouldContinueWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/init")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_ActuatorPath_ShouldContinueWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_SwaggerUiPath_ShouldContinueWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/swagger-ui/index.html")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_ApiDocsPath_ShouldContinueWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/v3/api-docs")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_PathEndingWithApiDocs_ShouldContinueWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/some-service/api-docs")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_NoAuthHeader_ShouldReturnUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/protected")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(filterChain);
    }

    @Test
    void filter_InvalidAuthHeader_ShouldReturnUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/protected")
                .header(HttpHeaders.AUTHORIZATION, "Basic sometoken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(filterChain);
    }

    @Test
    void filter_InvalidToken_ShouldReturnUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalidtoken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtService.isTokenValid("invalidtoken")).thenReturn(false);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(filterChain);
    }

    @Test
    void filter_ValidToken_ShouldAddHeadersAndContinue() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer validtoken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtService.isTokenValid("validtoken")).thenReturn(true);
        when(jwtService.extractUsername("validtoken")).thenReturn("testuser");
        when(jwtService.extractRole("validtoken")).thenReturn(UserRole.ADMIN);
        when(jwtService.extractUserId("validtoken")).thenReturn(1L);
        when(jwtService.extractEmployeeId("validtoken")).thenReturn(5L);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(argThat(ex -> {
            String userId = ex.getRequest().getHeaders().getFirst("X-User-Id");
            String userName = ex.getRequest().getHeaders().getFirst("X-User-Name");
            String userRole = ex.getRequest().getHeaders().getFirst("X-User-Role");
            String employeeId = ex.getRequest().getHeaders().getFirst("X-Employee-Id");
            return "1".equals(userId) &&
                    "testuser".equals(userName) &&
                    "ADMIN".equals(userRole) &&
                    "5".equals(employeeId);
        }));
    }

    @Test
    void filter_ValidTokenNoEmployeeId_ShouldAddEmptyEmployeeIdHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer validtoken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtService.isTokenValid("validtoken")).thenReturn(true);
        when(jwtService.extractUsername("validtoken")).thenReturn("testuser");
        when(jwtService.extractRole("validtoken")).thenReturn(UserRole.WAITER);
        when(jwtService.extractUserId("validtoken")).thenReturn(2L);
        when(jwtService.extractEmployeeId("validtoken")).thenReturn(null);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(argThat(ex -> {
            String employeeId = ex.getRequest().getHeaders().getFirst("X-Employee-Id");
            return "".equals(employeeId);
        }));
    }

    @Test
    void filter_TokenExtractionException_ShouldReturnUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer problematictoken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtService.isTokenValid("problematictoken")).thenThrow(new RuntimeException("Token error"));

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(filterChain);
    }

    @Test
    void getOrder_ShouldReturnNegativeValue() {
        assertEquals(-100, filter.getOrder());
    }

    @Test
    void filter_WebjarsPath_ShouldContinueWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/webjars/swagger-ui/swagger-ui.css")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_FaviconPath_ShouldContinueWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/favicon.ico")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_SwaggerConfigEndpoint_ShouldContinueWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/some-service/api-docs/swagger-config")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }
}
