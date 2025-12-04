package ru.ifmo.se.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ifmo.se.restaurant.dataaccess.KitchenDataAccess;
import ru.ifmo.se.restaurant.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.model.entity.KitchenQueue;
import ru.ifmo.se.restaurant.model.entity.Order;
import ru.ifmo.se.restaurant.model.entity.OrderItem;
import ru.ifmo.se.restaurant.model.DishStatus;
import ru.ifmo.se.restaurant.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KitchenService {
    private final KitchenDataAccess dataAccess;

    public KitchenService(KitchenDataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Transactional
    public void addOrderToKitchenQueue(Order order) {
        for (OrderItem item : order.getItems()) {
            KitchenQueue queueItem = new KitchenQueue();
            queueItem.setOrder(order);
            queueItem.setOrderItem(item);
            queueItem.setStatus(DishStatus.PENDING);
            queueItem.setCreatedAt(LocalDateTime.now());
            dataAccess.saveKitchenQueue(queueItem);
        }
    }

    public List<KitchenQueueDto> getKitchenQueue() {
        List<DishStatus> activeStatuses = Arrays.asList(DishStatus.PENDING, DishStatus.IN_PROGRESS);
        return dataAccess.findByStatusesOrderByCreatedAtAsc(activeStatuses)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public KitchenQueueDto updateDishStatus(Long queueId, DishStatus status) {
        KitchenQueue queueItem = dataAccess.findKitchenQueueById(queueId);

        queueItem.setStatus(status);
        
        if (status == DishStatus.IN_PROGRESS && queueItem.getStartedAt() == null) {
            queueItem.setStartedAt(LocalDateTime.now());
        }
        
        if (status == DishStatus.READY && queueItem.getCompletedAt() == null) {
            queueItem.setCompletedAt(LocalDateTime.now());
            checkAndUpdateOrderStatus(queueItem.getOrder());
        }

        return toDto(dataAccess.saveKitchenQueue(queueItem));
    }

    @Transactional
    private void checkAndUpdateOrderStatus(Order order) {
        List<KitchenQueue> queueItems = dataAccess.findByOrderId(order.getId());
        boolean allReady = queueItems.stream()
            .allMatch(item -> item.getStatus() == DishStatus.READY || item.getStatus() == DishStatus.SERVED);
        
        if (allReady && order.getStatus() == OrderStatus.IN_KITCHEN) {
            order.setStatus(OrderStatus.PREPARING);
            dataAccess.saveOrder(order);
        }
    }

    public Page<KitchenQueueDto> getAllKitchenQueueItems(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return dataAccess.findAllKitchenQueues(pageable).map(this::toDto);
    }

    private KitchenQueueDto toDto(KitchenQueue queue) {
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setId(queue.getId());
        dto.setOrderId(queue.getOrder().getId());
        dto.setOrderItemId(queue.getOrderItem().getId());
        dto.setDishName(queue.getOrderItem().getDish().getName());
        dto.setQuantity(queue.getOrderItem().getQuantity());
        dto.setStatus(queue.getStatus());
        dto.setSpecialRequest(queue.getOrderItem().getSpecialRequest());
        dto.setCreatedAt(queue.getCreatedAt());
        dto.setStartedAt(queue.getStartedAt());
        dto.setCompletedAt(queue.getCompletedAt());
        return dto;
    }
}

