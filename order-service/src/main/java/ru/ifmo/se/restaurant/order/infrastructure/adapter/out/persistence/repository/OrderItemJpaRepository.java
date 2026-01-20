package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity.OrderItemJpaEntity;

@Repository
public interface OrderItemJpaRepository extends ReactiveCrudRepository<OrderItemJpaEntity, Long> {
    Flux<OrderItemJpaEntity> findByOrderId(Long orderId);
    Mono<Void> deleteByOrderId(Long orderId);
}
