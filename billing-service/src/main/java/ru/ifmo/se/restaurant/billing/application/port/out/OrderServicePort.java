package ru.ifmo.se.restaurant.billing.application.port.out;

import ru.ifmo.se.restaurant.billing.application.dto.OrderDto;

public interface OrderServicePort {
    OrderDto getOrder(Long orderId);
}
