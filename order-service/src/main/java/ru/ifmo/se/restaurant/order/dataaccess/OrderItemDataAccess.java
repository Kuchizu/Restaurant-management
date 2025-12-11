package ru.ifmo.se.restaurant.order.dataaccess;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.entity.OrderItem;
import ru.ifmo.se.restaurant.order.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.repository.OrderItemRepository;

@Component
@RequiredArgsConstructor
public class OrderItemDataAccess {
    private final OrderItemRepository orderItemRepository;

    public Mono<OrderItem> findById(Long id) {
        return orderItemRepository.findById(id);
    }

    public Mono<OrderItem> getById(Long id) {
        return findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order item not found with id: " + id)));
    }

    public Flux<OrderItem> findByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public Flux<OrderItem> findAll() {
        return orderItemRepository.findAll();
    }

    public Mono<OrderItem> save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    public Mono<Void> deleteById(Long id) {
        return orderItemRepository.deleteById(id);
    }

    public Mono<Void> deleteByOrderId(Long orderId) {
        return orderItemRepository.deleteByOrderId(orderId);
    }
}
