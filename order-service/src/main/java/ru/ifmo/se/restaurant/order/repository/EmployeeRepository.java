package ru.ifmo.se.restaurant.order.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.order.entity.Employee;

@Repository
public interface EmployeeRepository extends ReactiveCrudRepository<Employee, Long> {
}
