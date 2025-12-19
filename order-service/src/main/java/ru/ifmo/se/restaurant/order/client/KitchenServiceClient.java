package ru.ifmo.se.restaurant.order.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.dto.KitchenQueueRequest;
import ru.ifmo.se.restaurant.order.exception.ServiceUnavailableException;

@Component
@Slf4j
public class KitchenServiceClient {
    private final WebClient webClient;

    public KitchenServiceClient(@LoadBalanced WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @CircuitBreaker(name = "kitchenService", fallbackMethod = "fallbackAddToQueue")
    public Mono<Void> addToKitchenQueue(KitchenQueueRequest request) {
        return webClient
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
