package ru.ifmo.se.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.model.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("""
        SELECT oi.dish.id as dishId, oi.dish.name as dishName, SUM(oi.quantity) as quantity
        FROM OrderItem oi
        JOIN oi.order o
        WHERE o.createdAt BETWEEN :startDate AND :endDate
        GROUP BY oi.dish.id, oi.dish.name
        ORDER BY SUM(oi.quantity) DESC
        """)
    List<Object[]> findPopularDishes(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT SUM(oi.dish.cost * oi.quantity)
        FROM OrderItem oi
        JOIN oi.order o
        WHERE o.createdAt BETWEEN :startDate AND :endDate
        """)
    BigDecimal calculateTotalCost(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT oi.dish.id as dishId, oi.dish.name as dishName,
               SUM(oi.price * oi.quantity) as revenue
        FROM OrderItem oi
        JOIN oi.order o
        WHERE o.createdAt BETWEEN :startDate AND :endDate
        GROUP BY oi.dish.id, oi.dish.name
        ORDER BY SUM(oi.price * oi.quantity) DESC
        """)
    List<Object[]> findDishesByRevenue(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
}

