package ru.ifmo.se.restaurant.order.application.port.out;

import ru.ifmo.se.restaurant.order.domain.entity.Order;
import ru.ifmo.se.restaurant.order.domain.entity.OrderItem;

import java.util.List;

public interface OrderEventPublisher {
    void publishOrderCreated(Order order, List<OrderItem> items);
    void publishOrderSentToKitchen(Order order, List<OrderItem> items);
}
