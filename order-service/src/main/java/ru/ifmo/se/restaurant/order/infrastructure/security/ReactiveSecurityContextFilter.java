package ru.ifmo.se.restaurant.order.infrastructure.security;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;

@Component
public class ReactiveSecurityContextFilter implements WebFilter {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/api-docs",
            "/webjars"
    );

    // Internal service-to-service endpoints (no auth required for GET)
    private static final List<String> INTERNAL_READ_PATHS = List.of(
            "/api/orders"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        HttpMethod method = exchange.getRequest().getMethod();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Allow internal service-to-service GET requests without auth
        if (isReadOnlyRequest(method) && isInternalReadPath(path)) {
            return chain.filter(exchange);
        }

        String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String usernameHeader = exchange.getRequest().getHeaders().getFirst("X-User-Name");
        String roleHeader = exchange.getRequest().getHeaders().getFirst("X-User-Role");
        String employeeIdHeader = exchange.getRequest().getHeaders().getFirst("X-Employee-Id");

        if (userIdHeader == null || roleHeader == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        UserContext userContext = new UserContext();
        userContext.setUserId(Long.parseLong(userIdHeader));
        userContext.setUsername(usernameHeader);
        userContext.setRole(roleHeader);
        if (employeeIdHeader != null && !employeeIdHeader.isEmpty()) {
            userContext.setEmployeeId(Long.parseLong(employeeIdHeader));
        }

        if (!userContext.hasRole("WAITER", "MANAGER", "ADMIN")) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange)
                .contextWrite(Context.of(UserContext.class, userContext));
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isInternalReadPath(String path) {
        return INTERNAL_READ_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isReadOnlyRequest(HttpMethod method) {
        return method == HttpMethod.GET || method == HttpMethod.HEAD || method == HttpMethod.OPTIONS;
    }

    public static Mono<UserContext> getCurrentUser() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(UserContext.class)) {
                return Mono.just(ctx.get(UserContext.class));
            }
            return Mono.empty();
        });
    }
}
