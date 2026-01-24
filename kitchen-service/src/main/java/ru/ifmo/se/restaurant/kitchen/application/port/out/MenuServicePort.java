package ru.ifmo.se.restaurant.kitchen.application.port.out;

import ru.ifmo.se.restaurant.kitchen.application.dto.DishInfoDto;

public interface MenuServicePort {
    DishInfoDto getDishByName(String name);
    DishInfoDto getDishById(Long id);
}
