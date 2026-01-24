package ru.ifmo.se.restaurant.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SwaggerConfigController {

    @GetMapping("/v3/api-docs/swagger-config")
    public Mono<Map<String, Object>> getSwaggerConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("configUrl", "/v3/api-docs/swagger-config");
        config.put("urls", List.of(
            Map.of("url", "/order-service/api-docs", "name", "Order Service"),
            Map.of("url", "/kitchen-service/api-docs", "name", "Kitchen Service"),
            Map.of("url", "/menu-service/api-docs", "name", "Menu Service"),
            Map.of("url", "/inventory-service/api-docs", "name", "Inventory Service"),
            Map.of("url", "/billing-service/api-docs", "name", "Billing Service"),
            Map.of("url", "/file-service/api-docs", "name", "File Service")
        ));
        config.put("urls.primaryName", "Order Service");
        return Mono.just(config);
    }
}
