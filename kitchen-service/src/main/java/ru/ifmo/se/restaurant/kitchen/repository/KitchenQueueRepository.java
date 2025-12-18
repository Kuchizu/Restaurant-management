package ru.ifmo.se.restaurant.kitchen.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.entity.KitchenQueue;

import java.util.List;

@Repository
public interface KitchenQueueRepository extends JpaRepository<KitchenQueue, Long> {
    List<KitchenQueue> findByStatusInOrderByCreatedAtAsc(List<DishStatus> statuses);
    List<KitchenQueue> findByOrderId(Long orderId);
    Page<KitchenQueue> findByStatus(DishStatus status, Pageable pageable);
}
