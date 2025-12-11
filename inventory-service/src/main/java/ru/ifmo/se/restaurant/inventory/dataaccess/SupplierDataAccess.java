package ru.ifmo.se.restaurant.inventory.dataaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.inventory.entity.Supplier;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.repository.SupplierRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierDataAccess {
    private final SupplierRepository supplierRepository;

    public Supplier save(Supplier supplier) {
        log.debug("Saving supplier: {}", supplier);
        return supplierRepository.save(supplier);
    }

    public Optional<Supplier> findById(Long id) {
        log.debug("Finding supplier by id: {}", id);
        return supplierRepository.findById(id);
    }

    public Supplier getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    public List<Supplier> findAll() {
        log.debug("Finding all suppliers");
        return supplierRepository.findAll();
    }

    public Page<Supplier> findAll(Pageable pageable) {
        log.debug("Finding all suppliers with pagination: {}", pageable);
        return supplierRepository.findAll(pageable);
    }

    public Slice<Supplier> findAllSlice(Pageable pageable) {
        log.debug("Finding all suppliers slice with pagination: {}", pageable);
        Page<Supplier> page = supplierRepository.findAll(pageable);
        return page;
    }

    public boolean existsById(Long id) {
        log.debug("Checking if supplier exists by id: {}", id);
        return supplierRepository.existsById(id);
    }

    public void deleteById(Long id) {
        log.debug("Deleting supplier by id: {}", id);
        supplierRepository.deleteById(id);
    }
}
