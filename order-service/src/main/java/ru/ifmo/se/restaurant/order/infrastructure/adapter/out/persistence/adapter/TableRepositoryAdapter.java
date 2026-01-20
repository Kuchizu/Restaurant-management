package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.application.port.out.TableRepositoryPort;
import ru.ifmo.se.restaurant.order.domain.entity.RestaurantTable;
import ru.ifmo.se.restaurant.order.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity.RestaurantTableJpaEntity;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.repository.RestaurantTableJpaRepository;

@Component
@RequiredArgsConstructor
public class TableRepositoryAdapter implements TableRepositoryPort {
    private final RestaurantTableJpaRepository tableJpaRepository;

    @Override
    public Mono<RestaurantTable> findById(Long id) {
        return tableJpaRepository.findById(id)
            .map(this::toDomain);
    }

    @Override
    public Mono<RestaurantTable> getById(Long id) {
        return findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Table not found with id: " + id)));
    }

    @Override
    public Flux<RestaurantTable> findAll(Pageable pageable) {
        return tableJpaRepository.findAll()
            .skip(pageable.getOffset())
            .take(pageable.getPageSize())
            .map(this::toDomain);
    }

    @Override
    public Mono<Long> count() {
        return tableJpaRepository.count();
    }

    @Override
    public Mono<RestaurantTable> save(RestaurantTable table) {
        return tableJpaRepository.save(toJpa(table))
            .map(this::toDomain);
    }

    private RestaurantTable toDomain(RestaurantTableJpaEntity jpaEntity) {
        return new RestaurantTable(
            jpaEntity.getId(),
            jpaEntity.getTableNumber(),
            jpaEntity.getCapacity(),
            jpaEntity.getLocation(),
            jpaEntity.getStatus()
        );
    }

    private RestaurantTableJpaEntity toJpa(RestaurantTable domain) {
        return new RestaurantTableJpaEntity(
            domain.getId(),
            domain.getTableNumber(),
            domain.getCapacity(),
            domain.getLocation(),
            domain.getStatus()
        );
    }
}
