package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.inventory.domain.valueobject.SupplyOrderStatus;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.SupplyOrderJpaEntity;

import java.util.List;

@Repository
public interface SupplyOrderJpaRepository extends JpaRepository<SupplyOrderJpaEntity, Long> {
    List<SupplyOrderJpaEntity> findByStatus(SupplyOrderStatus status);
}
