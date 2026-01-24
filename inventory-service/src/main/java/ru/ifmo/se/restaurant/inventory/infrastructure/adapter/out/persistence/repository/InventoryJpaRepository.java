package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.InventoryJpaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryJpaRepository extends JpaRepository<InventoryJpaEntity, Long> {
    Optional<InventoryJpaEntity> findByIngredientId(Long ingredientId);

    @Query("SELECT i FROM InventoryJpaEntity i WHERE i.quantity < i.minQuantity")
    List<InventoryJpaEntity> findLowStockItems();
}
