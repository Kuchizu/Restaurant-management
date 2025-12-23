package ru.ifmo.se.restaurant.kitchen.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.kitchen.dto.DishInfoDto;

@Slf4j
@Component
public class MenuServiceClientFallback implements MenuServiceClient {

    @Override
    public DishInfoDto getDishByName(String name) {
        log.warn("Menu service is unavailable, fallback for getDishByName: {}", name);
        return null;
    }

    @Override
    public DishInfoDto getDishById(Long id) {
        log.warn("Menu service is unavailable, fallback for getDishById: {}", id);
        return null;
    }
}
