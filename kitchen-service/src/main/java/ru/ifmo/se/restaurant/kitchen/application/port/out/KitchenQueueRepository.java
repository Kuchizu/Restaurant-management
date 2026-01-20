package ru.ifmo.se.restaurant.kitchen.application.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;

import java.util.List;
import java.util.Optional;

public interface KitchenQueueRepository {
    KitchenQueue save(KitchenQueue kitchenQueue);
    Optional<KitchenQueue> findById(Long id);
    List<KitchenQueue> findAll();
    List<KitchenQueue> findByStatusInOrderByCreatedAtAsc(List<DishStatus> statuses);
    List<KitchenQueue> findByOrderId(Long orderId);
    Page<KitchenQueue> findAll(Pageable pageable);
    Slice<KitchenQueue> findAllSlice(Pageable pageable);
    Page<KitchenQueue> findByStatus(DishStatus status, Pageable pageable);
    void deleteById(Long id);
    boolean existsById(Long id);
}
