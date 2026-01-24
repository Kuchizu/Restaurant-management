package ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity.DishJpaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishJpaRepository extends JpaRepository<DishJpaEntity, Long> {
    Optional<DishJpaEntity> findByName(String name);
    List<DishJpaEntity> findByIsActive(Boolean isActive);
    List<DishJpaEntity> findByCategoryId(Long categoryId);

    @Query("SELECT d FROM DishJpaEntity d WHERE d.isActive = true AND d.id = :id")
    Optional<DishJpaEntity> findActiveDishById(@Param("id") Long id);
}
