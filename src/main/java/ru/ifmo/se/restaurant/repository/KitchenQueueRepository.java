package ru.ifmo.se.restaurant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.model.entity.KitchenQueue;
import ru.ifmo.se.restaurant.model.DishStatus;

import java.util.List;

@Repository
public interface KitchenQueueRepository extends JpaRepository<KitchenQueue, Long> {
    Page<KitchenQueue> findAll(Pageable pageable);
    
    List<KitchenQueue> findByOrderId(Long orderId);
    
    List<KitchenQueue> findByStatus(DishStatus status);
    
    @Query("SELECT kq FROM KitchenQueue kq WHERE kq.status IN :statuses ORDER BY kq.createdAt ASC")
    List<KitchenQueue> findByStatusesOrderByCreatedAtAsc(@org.springframework.data.repository.query.Param("statuses") List<DishStatus> statuses);
}

