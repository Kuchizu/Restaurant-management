package ru.ifmo.se.restaurant.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.inventory.entity.SupplyOrder;
import ru.ifmo.se.restaurant.inventory.entity.SupplyOrderStatus;

import java.util.List;

@Repository
public interface SupplyOrderRepository extends JpaRepository<SupplyOrder, Long> {
    List<SupplyOrder> findByStatus(SupplyOrderStatus status);
    List<SupplyOrder> findBySupplierId(Long supplierId);
}
