package ru.ifmo.se.restaurant.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.billing.entity.Bill;
import ru.ifmo.se.restaurant.billing.entity.BillStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByOrderId(Long orderId);
    List<Bill> findByStatus(BillStatus status);
}
