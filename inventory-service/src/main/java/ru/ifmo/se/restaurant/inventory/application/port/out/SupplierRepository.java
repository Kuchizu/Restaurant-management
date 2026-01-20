package ru.ifmo.se.restaurant.inventory.application.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.inventory.domain.entity.Supplier;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository {
    Supplier save(Supplier supplier);
    Optional<Supplier> findById(Long id);
    List<Supplier> findAll();
    Page<Supplier> findAll(Pageable pageable);
    Slice<Supplier> findAllSlice(Pageable pageable);
    boolean existsById(Long id);
    void deleteById(Long id);
}
