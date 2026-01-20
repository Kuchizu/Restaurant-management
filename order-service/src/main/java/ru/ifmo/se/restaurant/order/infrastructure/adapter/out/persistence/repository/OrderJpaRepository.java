package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity.OrderJpaEntity;

@Repository
public interface OrderJpaRepository extends R2dbcRepository<OrderJpaEntity, Long> {
}
