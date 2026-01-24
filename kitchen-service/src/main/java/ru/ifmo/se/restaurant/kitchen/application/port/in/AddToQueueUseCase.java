package ru.ifmo.se.restaurant.kitchen.application.port.in;

import ru.ifmo.se.restaurant.kitchen.application.dto.KitchenQueueDto;

public interface AddToQueueUseCase {
    KitchenQueueDto addToQueue(KitchenQueueDto dto);
}
