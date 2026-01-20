package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity.EmployeeJpaEntity;

@Repository
public interface EmployeeJpaRepository extends R2dbcRepository<EmployeeJpaEntity, Long> {
}
