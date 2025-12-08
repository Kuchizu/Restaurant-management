package ru.ifmo.se.restaurant.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.inventory.entity.Inventory;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByIngredientId(Long ingredientId);
    List<Inventory> findByQuantityLessThanMinQuantity();
}
