package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ifmo.se.restaurant.kitchen.application.dto.DishInfoDto;

@FeignClient(name = "menu-service", fallback = MenuServiceFallback.class)
public interface MenuServiceFeignClient {
    @GetMapping("/api/dishes/by-name")
    DishInfoDto getDishByName(@RequestParam("name") String name);

    @GetMapping("/api/dishes/{id}")
    DishInfoDto getDishById(@PathVariable("id") Long id);
}
