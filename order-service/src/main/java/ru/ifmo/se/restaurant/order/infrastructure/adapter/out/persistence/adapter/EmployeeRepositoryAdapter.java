package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.application.port.out.EmployeeRepositoryPort;
import ru.ifmo.se.restaurant.order.domain.entity.Employee;
import ru.ifmo.se.restaurant.order.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity.EmployeeJpaEntity;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.repository.EmployeeJpaRepository;

@Component
@RequiredArgsConstructor
public class EmployeeRepositoryAdapter implements EmployeeRepositoryPort {
    private final EmployeeJpaRepository employeeJpaRepository;

    @Override
    public Mono<Employee> findById(Long id) {
        return employeeJpaRepository.findById(id)
            .map(this::toDomain);
    }

    @Override
    public Mono<Employee> getById(Long id) {
        return findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Employee not found with id: " + id)));
    }

    @Override
    public Flux<Employee> findAll(Pageable pageable) {
        return employeeJpaRepository.findAll()
            .skip(pageable.getOffset())
            .take(pageable.getPageSize())
            .map(this::toDomain);
    }

    @Override
    public Mono<Long> count() {
        return employeeJpaRepository.count();
    }

    @Override
    public Mono<Employee> save(Employee employee) {
        return employeeJpaRepository.save(toJpa(employee))
            .map(this::toDomain);
    }

    private Employee toDomain(EmployeeJpaEntity jpaEntity) {
        return new Employee(
            jpaEntity.getId(),
            jpaEntity.getFirstName(),
            jpaEntity.getLastName(),
            jpaEntity.getEmail(),
            jpaEntity.getPhone(),
            jpaEntity.getRole()
        );
    }

    private EmployeeJpaEntity toJpa(Employee domain) {
        return new EmployeeJpaEntity(
            domain.getId(),
            domain.getFirstName(),
            domain.getLastName(),
            domain.getEmail(),
            domain.getPhone(),
            domain.getRole()
        );
    }
}
