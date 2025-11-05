package ru.ifmo.se.restaurant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.model.entity.SupplyOrder;
import ru.ifmo.se.restaurant.model.SupplyOrderStatus;

import java.util.List;

@Repository
public interface SupplyOrderRepository extends JpaRepository<SupplyOrder, Long> {
    Page<SupplyOrder> findAll(Pageable pageable);
    
    Page<SupplyOrder> findBySupplierId(Long supplierId, Pageable pageable);
    
    List<SupplyOrder> findByStatus(SupplyOrderStatus status);
}

