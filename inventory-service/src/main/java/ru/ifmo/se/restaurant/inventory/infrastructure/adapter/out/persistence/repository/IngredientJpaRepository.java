package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.IngredientJpaEntity;

@Repository
public interface IngredientJpaRepository extends JpaRepository<IngredientJpaEntity, Long> {
}
