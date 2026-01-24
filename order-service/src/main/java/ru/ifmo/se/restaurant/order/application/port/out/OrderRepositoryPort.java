package ru.ifmo.se.restaurant.order.application.port.out;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.domain.entity.Order;

public interface OrderRepositoryPort {
    Mono<Order> findById(Long id);
    Mono<Order> getById(Long id);
    Flux<Order> findAll();
    Flux<Order> findAll(Pageable pageable);
    Mono<Long> count();
    Mono<Order> save(Order order);
    Mono<Void> deleteById(Long id);
}
