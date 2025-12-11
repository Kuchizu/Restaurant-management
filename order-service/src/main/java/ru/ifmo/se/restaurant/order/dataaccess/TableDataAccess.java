package ru.ifmo.se.restaurant.order.dataaccess;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.entity.RestaurantTable;
import ru.ifmo.se.restaurant.order.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.repository.TableRepository;

@Component
@RequiredArgsConstructor
public class TableDataAccess {
    private final TableRepository tableRepository;

    public Mono<RestaurantTable> findById(Long id) {
        return tableRepository.findById(id);
    }

    public Mono<RestaurantTable> getById(Long id) {
        return findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Table not found with id: " + id)));
    }

    public Flux<RestaurantTable> findAll() {
        return tableRepository.findAll();
    }

    public Flux<RestaurantTable> findAll(Pageable pageable) {
        // R2DBC doesn't have findAllBy(Pageable), so we manually apply pagination
        Flux<RestaurantTable> flux = tableRepository.findAll();

        // Apply sorting if present
        if (pageable.getSort().isSorted()) {
            return flux.collectList()
                .flatMapMany(tables -> {
                    tables.sort((t1, t2) -> t1.getId().compareTo(t2.getId())); // ASC by id
                    return Flux.fromIterable(tables);
                })
                .skip(pageable.getOffset())
                .take(pageable.getPageSize());
        }

        return flux.skip(pageable.getOffset()).take(pageable.getPageSize());
    }

    public Mono<Long> count() {
        return tableRepository.count();
    }

    public Mono<RestaurantTable> save(RestaurantTable table) {
        return tableRepository.save(table);
    }

    public Mono<Void> deleteById(Long id) {
        return tableRepository.deleteById(id);
    }
}
