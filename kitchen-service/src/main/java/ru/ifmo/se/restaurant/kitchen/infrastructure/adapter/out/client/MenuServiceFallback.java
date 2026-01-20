package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.kitchen.application.dto.DishInfoDto;
import ru.ifmo.se.restaurant.kitchen.infrastructure.exception.ServiceUnavailableException;

@Slf4j
@Component
public class MenuServiceFallback implements MenuServiceFeignClient {
    @Override
    public DishInfoDto getDishByName(String name) {
        log.error("Menu service is unavailable for dish name: {}", name);
        throw new ServiceUnavailableException(
            "Menu service is currently unavailable",
            "menu-service",
            "getDishByName"
        );
    }

    @Override
    public DishInfoDto getDishById(Long id) {
        log.error("Menu service is unavailable for dish ID: {}", id);
        throw new ServiceUnavailableException(
            "Menu service is currently unavailable",
            "menu-service",
            "getDishById"
        );
    }
}
