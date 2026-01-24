package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.inventory.application.port.out.SupplyOrderRepository;
import ru.ifmo.se.restaurant.inventory.domain.entity.SupplyOrder;
import ru.ifmo.se.restaurant.inventory.domain.valueobject.SupplyOrderStatus;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.SupplierJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.SupplyOrderJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.SupplierJpaRepository;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.SupplyOrderJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplyOrderRepositoryAdapter implements SupplyOrderRepository {

    private final SupplyOrderJpaRepository jpaRepository;
    private final SupplierJpaRepository supplierJpaRepository;

    @Override
    public SupplyOrder save(SupplyOrder supplyOrder) {
        log.debug("Saving supply order: {}", supplyOrder);
        SupplierJpaEntity supplierEntity = supplierJpaRepository.findById(supplyOrder.getSupplier().getId())
            .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

        SupplyOrderJpaEntity entity = SupplyOrderJpaEntity.fromDomain(supplyOrder, supplierEntity);
        SupplyOrderJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<SupplyOrder> findById(Long id) {
        log.debug("Finding supply order by id: {}", id);
        return jpaRepository.findById(id)
            .map(SupplyOrderJpaEntity::toDomain);
    }

    @Override
    public List<SupplyOrder> findAll() {
        log.debug("Finding all supply orders");
        return jpaRepository.findAll().stream()
            .map(SupplyOrderJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<SupplyOrder> findAll(Pageable pageable) {
        log.debug("Finding all supply orders with pagination: {}", pageable);
        return jpaRepository.findAll(pageable)
            .map(SupplyOrderJpaEntity::toDomain);
    }

    @Override
    public Slice<SupplyOrder> findAllSlice(Pageable pageable) {
        log.debug("Finding all supply orders slice with pagination: {}", pageable);
        Page<SupplyOrderJpaEntity> page = jpaRepository.findAll(pageable);
        return page.map(SupplyOrderJpaEntity::toDomain);
    }

    @Override
    public List<SupplyOrder> findByStatus(SupplyOrderStatus status) {
        log.debug("Finding supply orders by status: {}", status);
        return jpaRepository.findByStatus(status).stream()
            .map(SupplyOrderJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Checking if supply order exists by id: {}", id);
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting supply order by id: {}", id);
        jpaRepository.deleteById(id);
    }
}
