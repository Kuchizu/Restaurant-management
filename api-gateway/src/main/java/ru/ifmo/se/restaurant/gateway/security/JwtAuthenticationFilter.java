package ru.ifmo.se.restaurant.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.gateway.service.JwtService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter, Ordered {

    private final JwtService jwtService;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/init",
            "/actuator",
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/auth-api/api-docs",
            "/webjars",
            "/favicon.ico"
    );

    private static final List<String> PUBLIC_SUFFIXES = List.of(
            "/api-docs",
            "/api-docs/swagger-config"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtService.isTokenValid(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token).name();
            Long userId = jwtService.extractUserId(token);
            Long employeeId = jwtService.extractEmployeeId(token);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .header("X-Employee-Id", employeeId != null ? String.valueOf(employeeId) : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith)
                || PUBLIC_SUFFIXES.stream().anyMatch(path::endsWith);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
