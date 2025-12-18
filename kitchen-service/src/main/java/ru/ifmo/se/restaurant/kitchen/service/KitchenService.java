package ru.ifmo.se.restaurant.kitchen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.ifmo.se.restaurant.kitchen.dataaccess.KitchenQueueDataAccess;
import ru.ifmo.se.restaurant.kitchen.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.util.PaginationUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KitchenService {
    private final KitchenQueueDataAccess kitchenQueueDataAccess;

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
            return toDto(kitchenQueueDataAccess.save(queue));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // Get active kitchen queue (PENDING, IN_PROGRESS)
    public Flux<KitchenQueueDto> getActiveQueue() {
        return Mono.fromCallable(() -> {
            List<DishStatus> activeStatuses = Arrays.asList(DishStatus.PENDING, DishStatus.IN_PROGRESS);
            return kitchenQueueDataAccess.findByStatusInOrderByCreatedAtAsc(activeStatuses);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable)
        .map(this::toDto);
    }

    // Get all queue items
    public Flux<KitchenQueueDto> getAllQueue() {
        return Mono.fromCallable(kitchenQueueDataAccess::findAll)
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany(Flux::fromIterable)
            .map(this::toDto);
    }

    // Get queue item by ID
    public Mono<KitchenQueueDto> getQueueItemById(Long id) {
        return Mono.fromCallable(() -> kitchenQueueDataAccess.getById(id))
        .subscribeOn(Schedulers.boundedElastic())
        .map(this::toDto);
    }

    // Update dish status
    @Transactional
    public Mono<KitchenQueueDto> updateStatus(Long id, DishStatus status) {
        return Mono.fromCallable(() -> {
            KitchenQueue queue = kitchenQueueDataAccess.getById(id);

            queue.setStatus(status);

            if (status == DishStatus.IN_PROGRESS && queue.getStartedAt() == null) {
                queue.setStartedAt(LocalDateTime.now());
            }

            if (status == DishStatus.READY && queue.getCompletedAt() == null) {
                queue.setCompletedAt(LocalDateTime.now());
            }

            return toDto(kitchenQueueDataAccess.save(queue));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // Get queue items by order ID
    public Flux<KitchenQueueDto> getQueueByOrderId(Long orderId) {
        return Mono.fromCallable(() -> kitchenQueueDataAccess.findByOrderId(orderId))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany(Flux::fromIterable)
            .map(this::toDto);
    }

    // Get all queue items with pagination (Page)
    public Mono<Page<KitchenQueueDto>> getAllQueueItemsPaginated(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
            Page<KitchenQueue> queuePage = kitchenQueueDataAccess.findAll(pageable);
            return queuePage.map(this::toDto);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // Get all queue items with pagination (Slice for infinite scroll)
    public Mono<Slice<KitchenQueueDto>> getAllQueueItemsSlice(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
            Slice<KitchenQueue> queueSlice = kitchenQueueDataAccess.findAllSlice(pageable);
            return queueSlice.map(this::toDto);
        }).subscribeOn(Schedulers.boundedElastic());
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
