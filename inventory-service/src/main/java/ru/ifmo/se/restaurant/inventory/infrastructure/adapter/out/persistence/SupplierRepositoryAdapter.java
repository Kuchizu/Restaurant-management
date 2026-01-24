package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.inventory.application.port.out.SupplierRepository;
import ru.ifmo.se.restaurant.inventory.domain.entity.Supplier;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.SupplierJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.SupplierJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierRepositoryAdapter implements SupplierRepository {

    private final SupplierJpaRepository jpaRepository;

    @Override
    public Supplier save(Supplier supplier) {
        log.debug("Saving supplier: {}", supplier);
        SupplierJpaEntity entity = SupplierJpaEntity.fromDomain(supplier);
        SupplierJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Supplier> findById(Long id) {
        log.debug("Finding supplier by id: {}", id);
        return jpaRepository.findById(id)
            .map(SupplierJpaEntity::toDomain);
    }

    @Override
    public List<Supplier> findAll() {
        log.debug("Finding all suppliers");
        return jpaRepository.findAll().stream()
            .map(SupplierJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<Supplier> findAll(Pageable pageable) {
        log.debug("Finding all suppliers with pagination: {}", pageable);
        return jpaRepository.findAll(pageable)
            .map(SupplierJpaEntity::toDomain);
    }

    @Override
    public Slice<Supplier> findAllSlice(Pageable pageable) {
        log.debug("Finding all suppliers slice with pagination: {}", pageable);
        Page<SupplierJpaEntity> page = jpaRepository.findAll(pageable);
        return page.map(SupplierJpaEntity::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Checking if supplier exists by id: {}", id);
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting supplier by id: {}", id);
        jpaRepository.deleteById(id);
    }
}
