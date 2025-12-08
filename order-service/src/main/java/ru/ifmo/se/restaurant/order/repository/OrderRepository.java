package ru.ifmo.se.restaurant.order.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.order.entity.Order;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}
