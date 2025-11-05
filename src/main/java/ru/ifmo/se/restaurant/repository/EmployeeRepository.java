package ru.ifmo.se.restaurant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.model.entity.Employee;
import ru.ifmo.se.restaurant.model.EmployeeRole;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    
    Page<Employee> findByIsActiveTrue(Pageable pageable);
    
    Page<Employee> findByRoleAndIsActiveTrue(EmployeeRole role, Pageable pageable);
}

