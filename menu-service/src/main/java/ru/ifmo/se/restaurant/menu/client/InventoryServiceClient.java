package ru.ifmo.se.restaurant.menu.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.menu.dto.InventoryInfoDto;
import ru.ifmo.se.restaurant.menu.exception.ServiceUnavailableException;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class InventoryServiceClient {

    private final WebClient webClient;
    private static final String INVENTORY_SERVICE_URL = "http://inventory-service";

    public InventoryServiceClient(@LoadBalanced WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackGetAllInventory")
    public Mono<List<InventoryInfoDto>> getAllInventory() {
        log.debug("Fetching all inventory from inventory-service");
        return webClient
            .get()
            .uri(INVENTORY_SERVICE_URL + "/api/inventory")
            .retrieve()
            .onStatus(HttpStatusCode::is5xxServerError, response -> {
                log.error("Server error from inventory service: {}", response.statusCode());
                return Mono.error(new ServiceUnavailableException(
                    "Inventory service returned server error",
                    "inventory-service",
                    "getAllInventory"
                ));
            })
            .bodyToFlux(InventoryInfoDto.class)
            .collectList()
            .doOnError(error -> log.error("Error fetching inventory: {}", error.getMessage()));
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackGetLowStockInventory")
    public Mono<List<InventoryInfoDto>> getLowStockInventory() {
        log.debug("Fetching low stock inventory from inventory-service");
        return webClient
            .get()
            .uri(INVENTORY_SERVICE_URL + "/api/inventory/low-stock")
            .retrieve()
            .onStatus(HttpStatusCode::is5xxServerError, response -> {
                log.error("Server error from inventory service: {}", response.statusCode());
                return Mono.error(new ServiceUnavailableException(
                    "Inventory service returned server error",
                    "inventory-service",
                    "getLowStockInventory"
                ));
            })
            .bodyToFlux(InventoryInfoDto.class)
            .collectList()
            .doOnError(error -> log.error("Error fetching low stock: {}", error.getMessage()));
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackCheckIngredientAvailability")
    public Mono<Boolean> checkIngredientAvailability(Long ingredientId) {
        log.debug("Checking ingredient availability for ingredientId: {}", ingredientId);
        return webClient
            .get()
            .uri(INVENTORY_SERVICE_URL + "/api/inventory")
            .retrieve()
            .onStatus(HttpStatusCode::is5xxServerError, response ->
                Mono.error(new ServiceUnavailableException(
                    "Inventory service returned server error",
                    "inventory-service",
                    "checkIngredientAvailability"
                ))
            )
            .bodyToFlux(InventoryInfoDto.class)
            .filter(inv -> inv.getIngredientId().equals(ingredientId))
            .next()
            .map(inv -> inv.getQuantity().compareTo(inv.getMinQuantity()) > 0)
            .defaultIfEmpty(false)
            .doOnError(error -> log.error("Error checking ingredient availability: {}", error.getMessage()));
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackCheckAllIngredientsAvailable")
    public Mono<Boolean> checkAllIngredientsAvailable(List<Long> ingredientIds) {
        if (ingredientIds == null || ingredientIds.isEmpty()) {
            return Mono.just(true);
        }

        log.debug("Checking availability for ingredients: {}", ingredientIds);
        return webClient
            .get()
            .uri(INVENTORY_SERVICE_URL + "/api/inventory")
            .retrieve()
            .onStatus(HttpStatusCode::is5xxServerError, response ->
                Mono.error(new ServiceUnavailableException(
                    "Inventory service returned server error",
                    "inventory-service",
                    "checkAllIngredientsAvailable"
                ))
            )
            .bodyToFlux(InventoryInfoDto.class)
            .collectList()
            .map(inventoryList -> {
                for (Long ingredientId : ingredientIds) {
                    boolean found = inventoryList.stream()
                        .anyMatch(inv -> inv.getIngredientId().equals(ingredientId)
                                      && inv.getQuantity().compareTo(inv.getMinQuantity()) > 0);
                    if (!found) {
                        log.warn("Ingredient {} is not available or low in stock", ingredientId);
                        return false;
                    }
                }
                return true;
            })
            .doOnError(error -> log.error("Error checking ingredients availability: {}", error.getMessage()));
    }

    // Fallback methods
    private Mono<List<InventoryInfoDto>> fallbackGetAllInventory(Throwable throwable) {
        log.warn("Inventory service unavailable, returning empty list. Error: {}", throwable.getMessage());
        return Mono.just(Collections.emptyList());
    }

    private Mono<List<InventoryInfoDto>> fallbackGetLowStockInventory(Throwable throwable) {
        log.warn("Inventory service unavailable for low stock check. Error: {}", throwable.getMessage());
        return Mono.just(Collections.emptyList());
    }

    private Mono<Boolean> fallbackCheckIngredientAvailability(Long ingredientId, Throwable throwable) {
        log.warn("Inventory service unavailable, assuming ingredient {} is available. Error: {}",
                 ingredientId, throwable.getMessage());
        return Mono.just(true);
    }

    private Mono<Boolean> fallbackCheckAllIngredientsAvailable(List<Long> ingredientIds, Throwable throwable) {
        log.warn("Inventory service unavailable, assuming all ingredients are available. Error: {}",
                 throwable.getMessage());
        return Mono.just(true);
    }
}
