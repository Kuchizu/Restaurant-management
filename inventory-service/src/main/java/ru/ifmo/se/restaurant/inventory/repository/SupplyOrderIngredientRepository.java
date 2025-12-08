package ru.ifmo.se.restaurant.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.inventory.entity.SupplyOrderIngredient;

import java.util.List;

@Repository
public interface SupplyOrderIngredientRepository extends JpaRepository<SupplyOrderIngredient, Long> {
    List<SupplyOrderIngredient> findBySupplyOrderId(Long supplyOrderId);
}
