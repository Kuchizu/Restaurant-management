package ru.ifmo.se.restaurant.order.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.domain.entity.OrderItem;

public interface OrderItemRepositoryPort {
    Mono<OrderItem> findById(Long id);
    Mono<OrderItem> getById(Long id);
    Flux<OrderItem> findByOrderId(Long orderId);
    Mono<OrderItem> save(OrderItem orderItem);
    Mono<Void> deleteById(Long id);
}
