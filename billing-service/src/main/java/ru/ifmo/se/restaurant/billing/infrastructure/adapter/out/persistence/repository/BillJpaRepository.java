package ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;
import ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.persistence.entity.BillJpaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillJpaRepository extends JpaRepository<BillJpaEntity, Long> {
    Optional<BillJpaEntity> findByOrderId(Long orderId);
    List<BillJpaEntity> findByStatus(BillStatus status);
    Page<BillJpaEntity> findByStatus(BillStatus status, Pageable pageable);
}
