package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity.RestaurantTableJpaEntity;

@Repository
public interface RestaurantTableJpaRepository extends R2dbcRepository<RestaurantTableJpaEntity, Long> {
}
