package ru.ifmo.se.restaurant.order.dataaccess;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.entity.Order;
import ru.ifmo.se.restaurant.order.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.repository.OrderRepository;

@Component
@RequiredArgsConstructor
public class OrderDataAccess {
    private final OrderRepository orderRepository;

    public Mono<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Mono<Order> getById(Long id) {
        return findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order not found with id: " + id)));
    }

    public Flux<Order> findAll() {
        return orderRepository.findAll();
    }

    public Flux<Order> findAll(Pageable pageable) {
        // R2DBC doesn't have findAllBy(Pageable), so we manually apply pagination
        Flux<Order> flux = orderRepository.findAll();

        // Apply sorting if present
        if (pageable.getSort().isSorted()) {
            // For now, we'll fetch all and sort in memory
            // In production, you'd want to use @Query with ORDER BY
            return flux.collectList()
                .flatMapMany(orders -> {
                    orders.sort((o1, o2) -> {
                        // Simple descending sort by id
                        return o2.getId().compareTo(o1.getId());
                    });
                    return Flux.fromIterable(orders);
                })
                .skip(pageable.getOffset())
                .take(pageable.getPageSize());
        }

        return flux.skip(pageable.getOffset()).take(pageable.getPageSize());
    }

    public Mono<Long> count() {
        return orderRepository.count();
    }

    public Mono<Order> save(Order order) {
        return orderRepository.save(order);
    }

    public Mono<Void> deleteById(Long id) {
        return orderRepository.deleteById(id);
    }
}
