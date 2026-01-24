package ru.ifmo.se.restaurant.order.infrastructure.adapter.in.web.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.application.dto.DishResponse;
import ru.ifmo.se.restaurant.order.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.domain.exception.ServiceUnavailableException;

@Component
@Slf4j
public class MenuServiceClient {
    private final WebClient webClient;

    public MenuServiceClient(@LoadBalanced WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @CircuitBreaker(name = "menuService", fallbackMethod = "fallbackGetDish")
    public Mono<DishResponse> getDish(Long dishId) {
        return webClient
            .get()
            .uri("http://menu-service/api/dishes/{id}", dishId)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, response -> {
                if (response.statusCode().value() == 404) {
                    log.warn("Dish {} not found in menu service", dishId);
                    return Mono.error(new ResourceNotFoundException("Dish not found with id: " + dishId));
                }
                log.warn("Client error from menu service: {}", response.statusCode());
                return response.createException()
                    .flatMap(Mono::error);
            })
            .onStatus(HttpStatusCode::is5xxServerError, response -> {
                log.error("Server error from menu service: {}", response.statusCode());
                return Mono.error(new ServiceUnavailableException(
                    "Menu service returned server error",
                    "menu-service",
                    "getDish"
                ));
            })
            .bodyToMono(DishResponse.class)
            .doOnError(error -> log.error("Error calling menu service for dish {}: {}", dishId, error.getMessage()));
    }

    private Mono<DishResponse> fallbackGetDish(Long dishId, Throwable throwable) {
        log.error("Menu service unavailable for dish {}. Error: {}", dishId, throwable.getMessage());
        return Mono.error(new ServiceUnavailableException(
            "Menu service is currently unavailable",
            "menu-service",
            "getDish"
        ));
    }
}
