package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.kitchen.application.port.out.KitchenQueueRepository;
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;
import ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.persistence.entity.KitchenQueueJpaEntity;
import ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.persistence.repository.KitchenQueueJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KitchenQueueRepositoryAdapter implements KitchenQueueRepository {
    private final KitchenQueueJpaRepository jpaRepository;

    @Override
    public KitchenQueue save(KitchenQueue kitchenQueue) {
        log.debug("Saving kitchen queue: {}", kitchenQueue);
        KitchenQueueJpaEntity entity = KitchenQueueJpaEntity.fromDomain(kitchenQueue);
        KitchenQueueJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<KitchenQueue> findById(Long id) {
        log.debug("Finding kitchen queue by id: {}", id);
        return jpaRepository.findById(id)
                .map(KitchenQueueJpaEntity::toDomain);
    }

    @Override
    public List<KitchenQueue> findAll() {
        log.debug("Finding all kitchen queue items");
        return jpaRepository.findAll().stream()
                .map(KitchenQueueJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<KitchenQueue> findByStatusInOrderByCreatedAtAsc(List<DishStatus> statuses) {
        log.debug("Finding kitchen queue items by statuses: {}", statuses);
        return jpaRepository.findByStatusInOrderByCreatedAtAsc(statuses).stream()
                .map(KitchenQueueJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<KitchenQueue> findByOrderId(Long orderId) {
        log.debug("Finding kitchen queue items by orderId: {}", orderId);
        return jpaRepository.findByOrderId(orderId).stream()
                .map(KitchenQueueJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<KitchenQueue> findAll(Pageable pageable) {
        log.debug("Finding all kitchen queue items with pagination: {}", pageable);
        return jpaRepository.findAll(pageable)
                .map(KitchenQueueJpaEntity::toDomain);
    }

    @Override
    public Slice<KitchenQueue> findAllSlice(Pageable pageable) {
        log.debug("Finding all kitchen queue items as slice with pagination: {}", pageable);
        Page<KitchenQueueJpaEntity> page = jpaRepository.findAll(pageable);
        List<KitchenQueue> content = page.getContent().stream()
                .map(KitchenQueueJpaEntity::toDomain)
                .collect(Collectors.toList());
        boolean hasNext = page.hasNext();
        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Page<KitchenQueue> findByStatus(DishStatus status, Pageable pageable) {
        log.debug("Finding kitchen queue items by status: {} with pagination: {}", status, pageable);
        return jpaRepository.findByStatus(status, pageable)
                .map(KitchenQueueJpaEntity::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting kitchen queue by id: {}", id);
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Checking if kitchen queue exists by id: {}", id);
        return jpaRepository.existsById(id);
    }
}
