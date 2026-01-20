package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.application.port.out.OrderRepositoryPort;
import ru.ifmo.se.restaurant.order.domain.entity.Order;
import ru.ifmo.se.restaurant.order.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity.OrderJpaEntity;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.repository.OrderJpaRepository;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Mono<Order> findById(Long id) {
        return orderJpaRepository.findById(id)
            .map(this::toDomain);
    }

    @Override
    public Mono<Order> getById(Long id) {
        return findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order not found with id: " + id)));
    }

    @Override
    public Flux<Order> findAll() {
        return orderJpaRepository.findAll()
            .map(this::toDomain);
    }

    @Override
    public Flux<Order> findAll(Pageable pageable) {
        Flux<OrderJpaEntity> flux = orderJpaRepository.findAll();

        if (pageable.getSort().isSorted()) {
            return flux.collectList()
                .flatMapMany(orders -> {
                    orders.sort((o1, o2) -> o2.getId().compareTo(o1.getId()));
                    return Flux.fromIterable(orders);
                })
                .skip(pageable.getOffset())
                .take(pageable.getPageSize())
                .map(this::toDomain);
        }

        return flux.skip(pageable.getOffset())
            .take(pageable.getPageSize())
            .map(this::toDomain);
    }

    @Override
    public Mono<Long> count() {
        return orderJpaRepository.count();
    }

    @Override
    public Mono<Order> save(Order order) {
        return orderJpaRepository.save(toJpa(order))
            .map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return orderJpaRepository.deleteById(id);
    }

    private Order toDomain(OrderJpaEntity jpaEntity) {
        return new Order(
            jpaEntity.getId(),
            jpaEntity.getTableId(),
            jpaEntity.getWaiterId(),
            jpaEntity.getStatus(),
            jpaEntity.getTotalAmount(),
            jpaEntity.getSpecialRequests(),
            jpaEntity.getCreatedAt(),
            jpaEntity.getClosedAt(),
            jpaEntity.getVersion()
        );
    }

    private OrderJpaEntity toJpa(Order domain) {
        return new OrderJpaEntity(
            domain.getId(),
            domain.getTableId(),
            domain.getWaiterId(),
            domain.getStatus(),
            domain.getTotalAmount(),
            domain.getSpecialRequests(),
            domain.getCreatedAt(),
            domain.getClosedAt(),
            domain.getVersion()
        );
    }
}
