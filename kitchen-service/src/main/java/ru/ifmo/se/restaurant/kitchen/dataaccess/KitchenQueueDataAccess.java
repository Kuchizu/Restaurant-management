package ru.ifmo.se.restaurant.kitchen.dataaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.kitchen.repository.KitchenQueueRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KitchenQueueDataAccess {
    private final KitchenQueueRepository kitchenQueueRepository;

    public KitchenQueue save(KitchenQueue kitchenQueue) {
        log.debug("Saving kitchen queue: {}", kitchenQueue);
        return kitchenQueueRepository.save(kitchenQueue);
    }

    public Optional<KitchenQueue> findById(Long id) {
        log.debug("Finding kitchen queue by id: {}", id);
        return kitchenQueueRepository.findById(id);
    }

    public KitchenQueue getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen queue item not found with id: " + id));
    }

    public List<KitchenQueue> findAll() {
        log.debug("Finding all kitchen queue items");
        return kitchenQueueRepository.findAll();
    }

    public void deleteById(Long id) {
        log.debug("Deleting kitchen queue by id: {}", id);
        kitchenQueueRepository.deleteById(id);
    }

    public List<KitchenQueue> findByStatusInOrderByCreatedAtAsc(List<DishStatus> statuses) {
        log.debug("Finding kitchen queue items by statuses: {}", statuses);
        return kitchenQueueRepository.findByStatusInOrderByCreatedAtAsc(statuses);
    }

    public List<KitchenQueue> findByOrderId(Long orderId) {
        log.debug("Finding kitchen queue items by orderId: {}", orderId);
        return kitchenQueueRepository.findByOrderId(orderId);
    }

    public boolean existsById(Long id) {
        log.debug("Checking if kitchen queue exists by id: {}", id);
        return kitchenQueueRepository.existsById(id);
    }

    public Page<KitchenQueue> findAll(Pageable pageable) {
        log.debug("Finding all kitchen queue items with pagination: {}", pageable);
        return kitchenQueueRepository.findAll(pageable);
    }

    public Slice<KitchenQueue> findAllSlice(Pageable pageable) {
        log.debug("Finding all kitchen queue items as slice with pagination: {}", pageable);
        Page<KitchenQueue> page = kitchenQueueRepository.findAll(pageable);
        List<KitchenQueue> content = page.getContent();
        boolean hasNext = page.hasNext();
        return new SliceImpl<>(content, pageable, hasNext);
    }

    public Page<KitchenQueue> findByStatus(DishStatus status, Pageable pageable) {
        log.debug("Finding kitchen queue items by status: {} with pagination: {}", status, pageable);
        return kitchenQueueRepository.findByStatus(status, pageable);
    }
}
