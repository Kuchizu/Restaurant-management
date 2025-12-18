package ru.ifmo.se.restaurant.order.dataaccess;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.entity.Employee;
import ru.ifmo.se.restaurant.order.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.repository.EmployeeRepository;

@Component
@RequiredArgsConstructor
public class EmployeeDataAccess {
    private final EmployeeRepository employeeRepository;

    public Mono<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Mono<Employee> getById(Long id) {
        return findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Employee not found with id: " + id)));
    }

    public Flux<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Flux<Employee> findAll(Pageable pageable) {
        // R2DBC doesn't have findAllBy(Pageable), so we manually apply pagination
        Flux<Employee> flux = employeeRepository.findAll();

        // Apply sorting if present
        if (pageable.getSort().isSorted()) {
            return flux.collectList()
                .flatMapMany(employees -> {
                    employees.sort((e1, e2) -> e1.getId().compareTo(e2.getId())); // ASC by id
                    return Flux.fromIterable(employees);
                })
                .skip(pageable.getOffset())
                .take(pageable.getPageSize());
        }

        return flux.skip(pageable.getOffset()).take(pageable.getPageSize());
    }

    public Mono<Long> count() {
        return employeeRepository.count();
    }

    public Mono<Employee> save(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Mono<Void> deleteById(Long id) {
        return employeeRepository.deleteById(id);
    }
}
