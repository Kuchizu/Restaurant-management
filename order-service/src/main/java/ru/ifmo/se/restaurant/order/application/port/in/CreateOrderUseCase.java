package ru.ifmo.se.restaurant.order.application.port.in;

import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.application.dto.OrderDto;

public interface CreateOrderUseCase {
    Mono<OrderDto> createOrder(OrderDto dto);
}
