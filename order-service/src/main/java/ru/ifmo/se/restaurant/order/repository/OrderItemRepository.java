package ru.ifmo.se.restaurant.order.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.entity.OrderItem;

@Repository
public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {
    Flux<OrderItem> findByOrderId(Long orderId);
    Mono<Void> deleteByOrderId(Long orderId);
}
