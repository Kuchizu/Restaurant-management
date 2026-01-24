package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;
import ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.persistence.entity.KitchenQueueJpaEntity;

import java.util.List;

@Repository
public interface KitchenQueueJpaRepository extends JpaRepository<KitchenQueueJpaEntity, Long> {
    List<KitchenQueueJpaEntity> findByStatusInOrderByCreatedAtAsc(List<DishStatus> statuses);
    List<KitchenQueueJpaEntity> findByOrderId(Long orderId);
    Page<KitchenQueueJpaEntity> findByStatus(DishStatus status, Pageable pageable);
}
