package ru.ifmo.se.restaurant.order.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.dto.DishResponse;
import ru.ifmo.se.restaurant.order.exception.ServiceUnavailableException;

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
            .bodyToMono(DishResponse.class)
            .doOnError(error -> log.error("Error calling menu service: {}", error.getMessage()));
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
