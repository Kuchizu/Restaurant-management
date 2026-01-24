package ru.ifmo.se.restaurant.kitchen.application.port.out;

import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;

public interface KitchenEventPublisher {
    void publishDishReady(KitchenQueue kitchenQueue);
}
