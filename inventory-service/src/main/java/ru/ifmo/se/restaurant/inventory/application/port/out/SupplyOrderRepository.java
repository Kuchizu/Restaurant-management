package ru.ifmo.se.restaurant.inventory.application.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.inventory.domain.entity.SupplyOrder;
import ru.ifmo.se.restaurant.inventory.domain.valueobject.SupplyOrderStatus;

import java.util.List;
import java.util.Optional;

public interface SupplyOrderRepository {
    SupplyOrder save(SupplyOrder supplyOrder);
    Optional<SupplyOrder> findById(Long id);
    List<SupplyOrder> findAll();
    Page<SupplyOrder> findAll(Pageable pageable);
    Slice<SupplyOrder> findAllSlice(Pageable pageable);
    List<SupplyOrder> findByStatus(SupplyOrderStatus status);
    boolean existsById(Long id);
    void deleteById(Long id);
}
