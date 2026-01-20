package ru.ifmo.se.restaurant.order.application.port.in;

import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.application.dto.OrderDto;
import ru.ifmo.se.restaurant.order.application.dto.OrderItemDto;

public interface ManageOrderItemsUseCase {
    Mono<OrderDto> addItemToOrder(Long orderId, OrderItemDto itemDto);
    Mono<Void> removeItemFromOrder(Long orderId, Long itemId);
}
