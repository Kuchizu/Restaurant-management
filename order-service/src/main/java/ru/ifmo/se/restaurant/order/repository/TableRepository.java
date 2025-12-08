package ru.ifmo.se.restaurant.order.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.order.entity.RestaurantTable;

@Repository
public interface TableRepository extends ReactiveCrudRepository<RestaurantTable, Long> {
}
