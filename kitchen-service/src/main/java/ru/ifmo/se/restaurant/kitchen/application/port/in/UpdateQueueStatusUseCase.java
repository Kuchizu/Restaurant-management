package ru.ifmo.se.restaurant.kitchen.application.port.in;

import ru.ifmo.se.restaurant.kitchen.application.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;

public interface UpdateQueueStatusUseCase {
    KitchenQueueDto updateStatus(Long id, DishStatus status);
}
