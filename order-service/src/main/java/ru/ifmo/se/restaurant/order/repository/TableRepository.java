package ru.ifmo.se.restaurant.order.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.order.entity.RestaurantTable;

@Repository
public interface TableRepository extends R2dbcRepository<RestaurantTable, Long> {
}
