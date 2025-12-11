package ru.ifmo.se.restaurant.order.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.dto.KitchenQueueRequest;
import ru.ifmo.se.restaurant.order.exception.ServiceUnavailableException;

@Component
@RequiredArgsConstructor
@Slf4j
public class KitchenServiceClient {
    private final WebClient.Builder webClientBuilder;

    @CircuitBreaker(name = "kitchenService", fallbackMethod = "fallbackAddToQueue")
    public Mono<Void> addToKitchenQueue(KitchenQueueRequest request) {
        return webClientBuilder.build()
            .post()
            .uri("http://kitchen-service/api/kitchen/queue")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnError(error -> log.error("Error calling kitchen service: {}", error.getMessage()));
    }

    private Mono<Void> fallbackAddToQueue(KitchenQueueRequest request, Throwable throwable) {
        log.error("Kitchen service unavailable. Error: {}", throwable.getMessage());
        return Mono.error(new ServiceUnavailableException(
            "Kitchen service is currently unavailable",
            "kitchen-service",
            "addToQueue"
        ));
    }
}
