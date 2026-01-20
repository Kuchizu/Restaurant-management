package ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity.IngredientJpaEntity;

import java.util.Optional;

@Repository
public interface IngredientJpaRepository extends JpaRepository<IngredientJpaEntity, Long> {
    Optional<IngredientJpaEntity> findByName(String name);
}
