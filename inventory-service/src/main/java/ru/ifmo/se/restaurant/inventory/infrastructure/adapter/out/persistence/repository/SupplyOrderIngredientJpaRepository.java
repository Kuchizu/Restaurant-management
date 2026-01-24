package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.SupplyOrderIngredientJpaEntity;

import java.util.List;

@Repository
public interface SupplyOrderIngredientJpaRepository extends JpaRepository<SupplyOrderIngredientJpaEntity, Long> {
    List<SupplyOrderIngredientJpaEntity> findBySupplyOrderId(Long supplyOrderId);
}
