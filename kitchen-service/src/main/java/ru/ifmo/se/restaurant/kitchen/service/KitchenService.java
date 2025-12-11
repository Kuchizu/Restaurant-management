package ru.ifmo.se.restaurant.kitchen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.ifmo.se.restaurant.kitchen.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.kitchen.repository.KitchenQueueRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KitchenService {
    private final KitchenQueueRepository repository;

    // Add order items to kitchen queue
    @Transactional
    public Mono<KitchenQueueDto> addToQueue(KitchenQueueDto dto) {
        return Mono.fromCallable(() -> {
            KitchenQueue queue = new KitchenQueue();
            queue.setOrderId(dto.getOrderId());
            queue.setOrderItemId(dto.getOrderItemId());
            queue.setDishName(dto.getDishName());
            queue.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
            queue.setStatus(DishStatus.PENDING);
            queue.setSpecialRequest(dto.getSpecialRequest());
            queue.setCreatedAt(LocalDateTime.now());
            return toDto(repository.save(queue));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // Get active kitchen queue (PENDING, IN_PROGRESS)
    public Flux<KitchenQueueDto> getActiveQueue() {
        return Mono.fromCallable(() -> {
            List<DishStatus> activeStatuses = Arrays.asList(DishStatus.PENDING, DishStatus.IN_PROGRESS);
            return repository.findByStatusInOrderByCreatedAtAsc(activeStatuses);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable)
        .map(this::toDto);
    }

    // Get all queue items
    public Flux<KitchenQueueDto> getAllQueue() {
        return Mono.fromCallable(repository::findAll)
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany(Flux::fromIterable)
            .map(this::toDto);
    }

    // Get queue item by ID
    public Mono<KitchenQueueDto> getQueueItemById(Long id) {
        return Mono.fromCallable(() ->
            repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen queue item not found with id: " + id))
        )
        .subscribeOn(Schedulers.boundedElastic())
        .map(this::toDto);
    }

    // Update dish status
    @Transactional
    public Mono<KitchenQueueDto> updateStatus(Long id, DishStatus status) {
        return Mono.fromCallable(() -> {
            KitchenQueue queue = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen queue item not found with id: " + id));

            queue.setStatus(status);

            if (status == DishStatus.IN_PROGRESS && queue.getStartedAt() == null) {
                queue.setStartedAt(LocalDateTime.now());
            }

            if (status == DishStatus.READY && queue.getCompletedAt() == null) {
                queue.setCompletedAt(LocalDateTime.now());
            }

            return toDto(repository.save(queue));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // Get queue items by order ID
    public Flux<KitchenQueueDto> getQueueByOrderId(Long orderId) {
        return Mono.fromCallable(() -> repository.findByOrderId(orderId))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany(Flux::fromIterable)
            .map(this::toDto);
    }

    // Mapper
    private KitchenQueueDto toDto(KitchenQueue queue) {
        return new KitchenQueueDto(
            queue.getId(),
            queue.getOrderId(),
            queue.getOrderItemId(),
            queue.getDishName(),
            queue.getQuantity(),
            queue.getStatus(),
            queue.getSpecialRequest(),
            queue.getCreatedAt(),
            queue.getStartedAt(),
            queue.getCompletedAt()
        );
    }
}
