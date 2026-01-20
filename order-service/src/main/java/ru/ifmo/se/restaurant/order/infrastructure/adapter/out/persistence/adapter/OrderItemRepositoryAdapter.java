package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.application.port.out.OrderItemRepositoryPort;
import ru.ifmo.se.restaurant.order.domain.entity.OrderItem;
import ru.ifmo.se.restaurant.order.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity.OrderItemJpaEntity;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.repository.OrderItemJpaRepository;

@Component
@RequiredArgsConstructor
public class OrderItemRepositoryAdapter implements OrderItemRepositoryPort {
    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public Mono<OrderItem> findById(Long id) {
        return orderItemJpaRepository.findById(id)
            .map(this::toDomain);
    }

    @Override
    public Mono<OrderItem> getById(Long id) {
        return findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("OrderItem not found with id: " + id)));
    }

    @Override
    public Flux<OrderItem> findByOrderId(Long orderId) {
        return orderItemJpaRepository.findByOrderId(orderId)
            .map(this::toDomain);
    }

    @Override
    public Mono<OrderItem> save(OrderItem orderItem) {
        return orderItemJpaRepository.save(toJpa(orderItem))
            .map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return orderItemJpaRepository.deleteById(id);
    }

    private OrderItem toDomain(OrderItemJpaEntity jpaEntity) {
        return new OrderItem(
            jpaEntity.getId(),
            jpaEntity.getOrderId(),
            jpaEntity.getDishId(),
            jpaEntity.getDishName(),
            jpaEntity.getQuantity(),
            jpaEntity.getPrice(),
            jpaEntity.getSpecialRequest()
        );
    }

    private OrderItemJpaEntity toJpa(OrderItem domain) {
        return new OrderItemJpaEntity(
            domain.getId(),
            domain.getOrderId(),
            domain.getDishId(),
            domain.getDishName(),
            domain.getQuantity(),
            domain.getPrice(),
            domain.getSpecialRequest()
        );
    }
}
