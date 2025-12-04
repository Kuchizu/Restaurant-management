package ru.ifmo.se.restaurant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.model.entity.Order;
import ru.ifmo.se.restaurant.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"table", "waiter", "items"})
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"table", "waiter"})
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"table", "waiter"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"table", "waiter"})
    Page<Order> findByTableId(Long tableId, Pageable pageable);

    @EntityGraph(attributePaths = {"table", "waiter"})
    Page<Order> findByWaiterId(Long waiterId, Pageable pageable);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.table LEFT JOIN FETCH o.waiter WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.dish WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByDateRangeWithItems(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @EntityGraph(attributePaths = {"table", "waiter"})
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    List<Order> findByStatuses(@Param("statuses") List<OrderStatus> statuses);
}

