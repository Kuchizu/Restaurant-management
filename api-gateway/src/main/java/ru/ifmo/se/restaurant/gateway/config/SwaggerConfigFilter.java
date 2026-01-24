package ru.ifmo.se.restaurant.gateway.config;

import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class SwaggerConfigFilter implements WebFilter, Ordered {

    private static final String SWAGGER_CONFIG = """
            {
              "configUrl": "/v3/api-docs/swagger-config",
              "persistAuthorization": true,
              "urls": [
                {"name": "Auth (Gateway)", "url": "/auth-api/api-docs"},
                {"name": "Order Service", "url": "/order-service/api-docs"},
                {"name": "Kitchen Service", "url": "/kitchen-service/api-docs"},
                {"name": "Menu Service", "url": "/menu-service/api-docs"},
                {"name": "Inventory Service", "url": "/inventory-service/api-docs"},
                {"name": "Billing Service", "url": "/billing-service/api-docs"},
                {"name": "File Service", "url": "/file-service/api-docs"}
              ],
              "urls.primaryName": "Auth (Gateway)"
            }
            """;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if ("/v3/api-docs/swagger-config".equals(path)) {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Headers", "*");

            byte[] bytes = SWAGGER_CONFIG.getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
