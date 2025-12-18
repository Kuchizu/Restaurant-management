package ru.ifmo.se.restaurant.inventory.dataaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.inventory.entity.SupplyOrder;
import ru.ifmo.se.restaurant.inventory.entity.SupplyOrderStatus;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.repository.SupplyOrderRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplyOrderDataAccess {
    private final SupplyOrderRepository supplyOrderRepository;

    public SupplyOrder save(SupplyOrder supplyOrder) {
        log.debug("Saving supply order: {}", supplyOrder);
        return supplyOrderRepository.save(supplyOrder);
    }

    public Optional<SupplyOrder> findById(Long id) {
        log.debug("Finding supply order by id: {}", id);
        return supplyOrderRepository.findById(id);
    }

    public SupplyOrder getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supply order not found"));
    }

    public List<SupplyOrder> findAll() {
        log.debug("Finding all supply orders");
        return supplyOrderRepository.findAll();
    }

    public Page<SupplyOrder> findAll(Pageable pageable) {
        log.debug("Finding all supply orders with pagination: {}", pageable);
        return supplyOrderRepository.findAll(pageable);
    }

    public Slice<SupplyOrder> findAllSlice(Pageable pageable) {
        log.debug("Finding all supply orders slice with pagination: {}", pageable);
        Page<SupplyOrder> page = supplyOrderRepository.findAll(pageable);
        return page;
    }

    public List<SupplyOrder> findByStatus(SupplyOrderStatus status) {
        log.debug("Finding supply orders by status: {}", status);
        return supplyOrderRepository.findByStatus(status);
    }

    public List<SupplyOrder> findBySupplierId(Long supplierId) {
        log.debug("Finding supply orders by supplierId: {}", supplierId);
        return supplyOrderRepository.findBySupplierId(supplierId);
    }

    public boolean existsById(Long id) {
        log.debug("Checking if supply order exists by id: {}", id);
        return supplyOrderRepository.existsById(id);
    }

    public void deleteById(Long id) {
        log.debug("Deleting supply order by id: {}", id);
        supplyOrderRepository.deleteById(id);
    }
}
