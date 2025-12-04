package ru.ifmo.se.restaurant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.model.entity.Bill;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    @EntityGraph(attributePaths = {"order", "order.table", "order.waiter"})
    Optional<Bill> findById(Long id);

    @EntityGraph(attributePaths = {"order", "order.table", "order.waiter"})
    Optional<Bill> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"order"})
    Page<Bill> findAll(Pageable pageable);

    @Query("SELECT SUM(b.total) FROM Bill b WHERE b.issuedAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalRevenue(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}

