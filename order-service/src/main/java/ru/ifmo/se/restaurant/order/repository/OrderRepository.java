package ru.ifmo.se.restaurant.order.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.order.entity.Order;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {
}
