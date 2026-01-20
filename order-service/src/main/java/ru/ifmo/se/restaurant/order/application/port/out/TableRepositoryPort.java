package ru.ifmo.se.restaurant.order.application.port.out;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.domain.entity.RestaurantTable;

public interface TableRepositoryPort {
    Mono<RestaurantTable> findById(Long id);
    Mono<RestaurantTable> getById(Long id);
    Flux<RestaurantTable> findAll(Pageable pageable);
    Mono<Long> count();
    Mono<RestaurantTable> save(RestaurantTable table);
}
