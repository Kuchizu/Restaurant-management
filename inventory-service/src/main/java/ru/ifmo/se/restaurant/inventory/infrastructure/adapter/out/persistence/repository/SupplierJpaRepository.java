package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.SupplierJpaEntity;

@Repository
public interface SupplierJpaRepository extends JpaRepository<SupplierJpaEntity, Long> {
}
