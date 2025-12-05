package ru.ifmo.se.restaurant.dataaccess;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.model.entity.Ingredient;
import ru.ifmo.se.restaurant.model.entity.Inventory;
import ru.ifmo.se.restaurant.repository.IngredientRepository;
import ru.ifmo.se.restaurant.repository.InventoryRepository;

import java.time.LocalDate;
import java.util.List;

@Component
public class InventoryDataAccess {
    private final InventoryRepository inventoryRepository;
    private final IngredientRepository ingredientRepository;

    public InventoryDataAccess(InventoryRepository inventoryRepository,
                              IngredientRepository ingredientRepository) {
        this.inventoryRepository = inventoryRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public Ingredient findIngredientById(Long id) {
        return ingredientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + id));
    }

    public Inventory saveInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public List<Inventory> findAvailableForReservation(Long ingredientId, Integer requiredQuantity, LocalDate current)
    {
        return inventoryRepository.findAvailableForReservation(ingredientId, requiredQuantity, current);
    }

    public List<Inventory> findByIngredientId(Long ingredientId) {
        return inventoryRepository.findByIngredientId(ingredientId);
    }

    public Inventory findInventoryById(Long id) {
        return inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
    }

    public Page<Inventory> findAllInventory(Pageable pageable) {
        return inventoryRepository.findAll(pageable);
    }

    public List<Inventory> findExpiringSoon(LocalDate date) {
        return inventoryRepository.findExpiringSoon(date);
    }

    public void deleteInventory(Inventory inventory) {
        inventoryRepository.delete(inventory);
    }
}
