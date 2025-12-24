package ru.ifmo.se.restaurant.kitchen.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.kitchen.client.MenuServiceClient;
import ru.ifmo.se.restaurant.kitchen.dataaccess.KitchenQueueDataAccess;
import ru.ifmo.se.restaurant.kitchen.dto.DishInfoDto;
import ru.ifmo.se.restaurant.kitchen.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.util.PaginationUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KitchenService {
    private final KitchenQueueDataAccess kitchenQueueDataAccess;
    private final MenuServiceClient menuServiceClient;

    // Add order items to kitchen queue
    @Transactional
    public KitchenQueueDto addToQueue(KitchenQueueDto dto) {
        // Validate dish exists in menu service
        DishInfoDto dishInfo = menuServiceClient.getDishByName(dto.getDishName());
        log.info("Dish info from menu-service: {} (category: {}, price: {})",
                 dishInfo.getName(), dishInfo.getCategoryName(), dishInfo.getPrice());

        KitchenQueue queue = new KitchenQueue();
        queue.setOrderId(dto.getOrderId());
        queue.setOrderItemId(dto.getOrderItemId());
        queue.setDishName(dto.getDishName());
        queue.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
        queue.setStatus(DishStatus.PENDING);
        queue.setSpecialRequest(dto.getSpecialRequest());
        queue.setCreatedAt(LocalDateTime.now());
        return toDto(kitchenQueueDataAccess.save(queue));
    }

    // Get active kitchen queue (PENDING, IN_PROGRESS)
    public List<KitchenQueueDto> getActiveQueue() {
        List<DishStatus> activeStatuses = Arrays.asList(DishStatus.PENDING, DishStatus.IN_PROGRESS);
        return kitchenQueueDataAccess.findByStatusInOrderByCreatedAtAsc(activeStatuses)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Get all queue items
    public List<KitchenQueueDto> getAllQueue() {
        return kitchenQueueDataAccess.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Get queue item by ID
    public KitchenQueueDto getQueueItemById(Long id) {
        return toDto(kitchenQueueDataAccess.getById(id));
    }

    // Update dish status
    @Transactional
    public KitchenQueueDto updateStatus(Long id, DishStatus status) {
        KitchenQueue queue = kitchenQueueDataAccess.getById(id);

        queue.setStatus(status);

        if (status == DishStatus.IN_PROGRESS && queue.getStartedAt() == null) {
            queue.setStartedAt(LocalDateTime.now());
        }

        if (status == DishStatus.READY && queue.getCompletedAt() == null) {
            queue.setCompletedAt(LocalDateTime.now());
        }

        return toDto(kitchenQueueDataAccess.save(queue));
    }

    // Get queue items by order ID
    public List<KitchenQueueDto> getQueueByOrderId(Long orderId) {
        return kitchenQueueDataAccess.findByOrderId(orderId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Get all queue items with pagination (Page)
    public Page<KitchenQueueDto> getAllQueueItemsPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<KitchenQueue> queuePage = kitchenQueueDataAccess.findAll(pageable);
        return queuePage.map(this::toDto);
    }

    // Get all queue items with pagination (Slice for infinite scroll)
    public Slice<KitchenQueueDto> getAllQueueItemsSlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Slice<KitchenQueue> queueSlice = kitchenQueueDataAccess.findAllSlice(pageable);
        return queueSlice.map(this::toDto);
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
