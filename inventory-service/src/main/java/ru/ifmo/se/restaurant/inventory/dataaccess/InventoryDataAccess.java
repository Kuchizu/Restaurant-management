package ru.ifmo.se.restaurant.inventory.dataaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.inventory.entity.Inventory;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.repository.InventoryRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryDataAccess {
    private final InventoryRepository inventoryRepository;

    public Inventory save(Inventory inventory) {
        log.debug("Saving inventory: {}", inventory);
        return inventoryRepository.save(inventory);
    }

    public Optional<Inventory> findById(Long id) {
        log.debug("Finding inventory by id: {}", id);
        return inventoryRepository.findById(id);
    }

    public Inventory getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    }

    public List<Inventory> findAll() {
        log.debug("Finding all inventory items");
        return inventoryRepository.findAll();
    }

    public Page<Inventory> findAll(Pageable pageable) {
        log.debug("Finding all inventory items with pagination: {}", pageable);
        return inventoryRepository.findAll(pageable);
    }

    public Slice<Inventory> findAllSlice(Pageable pageable) {
        log.debug("Finding all inventory items slice with pagination: {}", pageable);
        Page<Inventory> page = inventoryRepository.findAll(pageable);
        return page;
    }

    public Optional<Inventory> findByIngredientId(Long ingredientId) {
        log.debug("Finding inventory by ingredientId: {}", ingredientId);
        return inventoryRepository.findByIngredientId(ingredientId);
    }

    public List<Inventory> findLowStockItems() {
        log.debug("Finding low stock inventory items");
        return inventoryRepository.findLowStockItems();
    }

    public boolean existsById(Long id) {
        log.debug("Checking if inventory exists by id: {}", id);
        return inventoryRepository.existsById(id);
    }

    public void deleteById(Long id) {
        log.debug("Deleting inventory by id: {}", id);
        inventoryRepository.deleteById(id);
    }
}
