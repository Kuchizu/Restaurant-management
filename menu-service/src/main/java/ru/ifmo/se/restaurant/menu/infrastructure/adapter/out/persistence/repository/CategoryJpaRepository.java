package ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity.CategoryJpaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {
    Optional<CategoryJpaEntity> findByName(String name);
    List<CategoryJpaEntity> findByIsActive(Boolean isActive);
}
