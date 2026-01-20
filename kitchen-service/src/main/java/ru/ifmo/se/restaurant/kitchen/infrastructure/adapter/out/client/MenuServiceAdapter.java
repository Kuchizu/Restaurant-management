package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.kitchen.application.dto.DishInfoDto;
import ru.ifmo.se.restaurant.kitchen.application.port.out.MenuServicePort;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuServiceAdapter implements MenuServicePort {
    private final MenuServiceFeignClient feignClient;

    @Override
    public DishInfoDto getDishByName(String name) {
        return feignClient.getDishByName(name);
    }

    @Override
    public DishInfoDto getDishById(Long id) {
        return feignClient.getDishById(id);
    }
}
