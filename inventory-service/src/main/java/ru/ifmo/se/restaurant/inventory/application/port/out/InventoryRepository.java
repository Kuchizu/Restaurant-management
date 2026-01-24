package ru.ifmo.se.restaurant.inventory.application.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.inventory.domain.entity.Inventory;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    Inventory save(Inventory inventory);
    Optional<Inventory> findById(Long id);
    List<Inventory> findAll();
    Page<Inventory> findAll(Pageable pageable);
    Slice<Inventory> findAllSlice(Pageable pageable);
    Optional<Inventory> findByIngredientId(Long ingredientId);
    List<Inventory> findLowStockItems();
    boolean existsById(Long id);
    void deleteById(Long id);
}
