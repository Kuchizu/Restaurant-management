package ru.ifmo.se.restaurant.kitchen.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.kitchen.application.dto.DishInfoDto;
import ru.ifmo.se.restaurant.kitchen.application.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.application.port.in.AddToQueueUseCase;
import ru.ifmo.se.restaurant.kitchen.application.port.in.GetQueueUseCase;
import ru.ifmo.se.restaurant.kitchen.application.port.in.UpdateQueueStatusUseCase;
import ru.ifmo.se.restaurant.kitchen.application.port.out.KitchenEventPublisher;
import ru.ifmo.se.restaurant.kitchen.application.port.out.KitchenQueueRepository;
import ru.ifmo.se.restaurant.kitchen.application.port.out.MenuServicePort;
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.domain.exception.KitchenQueueNotFoundException;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;
import ru.ifmo.se.restaurant.kitchen.util.PaginationUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KitchenService implements AddToQueueUseCase, GetQueueUseCase, UpdateQueueStatusUseCase {
    private final KitchenQueueRepository kitchenQueueRepository;
    private final MenuServicePort menuServicePort;
    private final KitchenEventPublisher kitchenEventPublisher;

    @Override
    public KitchenQueueDto addToQueue(KitchenQueueDto dto) {
        // Validate dish exists in menu service
        DishInfoDto dishInfo = menuServicePort.getDishByName(dto.getDishName());
        log.info("Dish info from menu-service: {} (category: {}, price: {})",
                 dishInfo.getName(), dishInfo.getCategoryName(), dishInfo.getPrice());

        KitchenQueue queue = KitchenQueue.builder()
                .orderId(dto.getOrderId())
                .orderItemId(dto.getOrderItemId())
                .dishName(dto.getDishName())
                .quantity(dto.getQuantity() != null ? dto.getQuantity() : 1)
                .status(DishStatus.PENDING)
                .specialRequest(dto.getSpecialRequest())
                .createdAt(LocalDateTime.now())
                .build();

        KitchenQueue saved = kitchenQueueRepository.save(queue);
        return KitchenQueueDto.fromDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenQueueDto> getActiveQueue() {
        List<DishStatus> activeStatuses = Arrays.asList(DishStatus.PENDING, DishStatus.IN_PROGRESS);
        return kitchenQueueRepository.findByStatusInOrderByCreatedAtAsc(activeStatuses)
                .stream()
                .map(KitchenQueueDto::fromDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenQueueDto> getAllQueue() {
        return kitchenQueueRepository.findAll()
                .stream()
                .map(KitchenQueueDto::fromDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public KitchenQueueDto getQueueItemById(Long id) {
        KitchenQueue queue = kitchenQueueRepository.findById(id)
                .orElseThrow(() -> new KitchenQueueNotFoundException(id));
        return KitchenQueueDto.fromDomain(queue);
    }

    @Override
    public KitchenQueueDto updateStatus(Long id, DishStatus status) {
        KitchenQueue queue = kitchenQueueRepository.findById(id)
                .orElseThrow(() -> new KitchenQueueNotFoundException(id));

        KitchenQueue updatedQueue = queue.withStatus(status);
        KitchenQueue saved = kitchenQueueRepository.save(updatedQueue);

        // Publish DISH_READY event when dish is ready
        if (status == DishStatus.READY) {
            log.info("Publishing DISH_READY event for order: {}, dish: {}", saved.getOrderId(), saved.getDishName());
            kitchenEventPublisher.publishDishReady(saved);
        }

        return KitchenQueueDto.fromDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenQueueDto> getQueueByOrderId(Long orderId) {
        return kitchenQueueRepository.findByOrderId(orderId)
                .stream()
                .map(KitchenQueueDto::fromDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KitchenQueueDto> getAllQueueItemsPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<KitchenQueue> queuePage = kitchenQueueRepository.findAll(pageable);
        return queuePage.map(KitchenQueueDto::fromDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<KitchenQueueDto> getAllQueueItemsSlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Slice<KitchenQueue> queueSlice = kitchenQueueRepository.findAllSlice(pageable);
        return queueSlice.map(KitchenQueueDto::fromDomain);
    }
}
