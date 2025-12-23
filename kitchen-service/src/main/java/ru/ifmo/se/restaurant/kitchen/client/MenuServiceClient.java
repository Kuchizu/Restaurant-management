package ru.ifmo.se.restaurant.kitchen.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.ifmo.se.restaurant.kitchen.dto.DishInfoDto;
import ru.ifmo.se.restaurant.kitchen.exception.ServiceUnavailableException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MenuServiceClient {

    private final RestTemplate restTemplate;
    private static final String MENU_SERVICE_URL = "http://menu-service";

    @CircuitBreaker(name = "menuService", fallbackMethod = "fallbackGetDishByName")
    public Optional<DishInfoDto> getDishByName(String dishName) {
        log.debug("Fetching dish info from menu-service for dish: {}", dishName);
        try {
            DishInfoDto[] dishes = restTemplate.getForObject(
                MENU_SERVICE_URL + "/api/dishes?name=" + dishName,
                DishInfoDto[].class
            );
            if (dishes != null && dishes.length > 0) {
                return Optional.of(dishes[0]);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching dish info: {}", e.getMessage());
            throw new ServiceUnavailableException(
                "Menu service is unavailable",
                "menu-service",
                "getDishByName"
            );
        }
    }

    @CircuitBreaker(name = "menuService", fallbackMethod = "fallbackGetDishById")
    public Optional<DishInfoDto> getDishById(Long dishId) {
        log.debug("Fetching dish info from menu-service for dishId: {}", dishId);
        try {
            DishInfoDto dish = restTemplate.getForObject(
                MENU_SERVICE_URL + "/api/dishes/" + dishId,
                DishInfoDto.class
            );
            return Optional.ofNullable(dish);
        } catch (Exception e) {
            log.error("Error fetching dish info: {}", e.getMessage());
            throw new ServiceUnavailableException(
                "Menu service is unavailable",
                "menu-service",
                "getDishById"
            );
        }
    }

    private Optional<DishInfoDto> fallbackGetDishByName(String dishName, Throwable throwable) {
        log.warn("Menu service unavailable, using fallback for dish: {}. Error: {}",
                 dishName, throwable.getMessage());
        return Optional.empty();
    }

    private Optional<DishInfoDto> fallbackGetDishById(Long dishId, Throwable throwable) {
        log.warn("Menu service unavailable, using fallback for dishId: {}. Error: {}",
                 dishId, throwable.getMessage());
        return Optional.empty();
    }
}
