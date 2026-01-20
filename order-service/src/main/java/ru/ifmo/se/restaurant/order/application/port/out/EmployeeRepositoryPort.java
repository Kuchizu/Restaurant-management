package ru.ifmo.se.restaurant.order.application.port.out;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.domain.entity.Employee;

public interface EmployeeRepositoryPort {
    Mono<Employee> findById(Long id);
    Mono<Employee> getById(Long id);
    Flux<Employee> findAll(Pageable pageable);
    Mono<Long> count();
    Mono<Employee> save(Employee employee);
}
