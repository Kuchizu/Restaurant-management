package ru.ifmo.se.restaurant.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.dto.EmployeeDto;
import ru.ifmo.se.restaurant.model.EmployeeRole;
import ru.ifmo.se.restaurant.repository.EmployeeRepository;

@RestController
@RequestMapping("/api/employees")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Employee Management", description = "API for managing employees")
public class EmployeeController {
    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new employee")
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeDto dto) {
        ru.ifmo.se.restaurant.model.entity.Employee employee = new ru.ifmo.se.restaurant.model.entity.Employee();
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setRole(dto.getRole());
        employee.setIsActive(true);
        employee = employeeRepository.save(employee);
        return new ResponseEntity<>(toDto(employee), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get employee by ID")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable @NonNull Long id) {
        ru.ifmo.se.restaurant.model.entity.Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ru.ifmo.se.restaurant.exception.ResourceNotFoundException("Employee not found with id: " + id));
        return ResponseEntity.ok(toDto(employee));
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all employees with pagination")
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<EmployeeDto> result = employeeRepository.findByIsActiveTrue(
            org.springframework.data.domain.PageRequest.of(page, Math.min(size, 50)))
            .map(this::toDto);
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(result.getTotalElements()))
            .body(result);
    }

    @GetMapping("/role/{role}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get employees by role")
    public ResponseEntity<Page<EmployeeDto>> getEmployeesByRole(
            @PathVariable EmployeeRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(employeeRepository.findByRoleAndIsActiveTrue(role,
            org.springframework.data.domain.PageRequest.of(page, Math.min(size, 50)))
            .map(this::toDto));
    }

    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update employee")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable @NonNull Long id, @Valid @RequestBody EmployeeDto dto) {
        ru.ifmo.se.restaurant.model.entity.Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ru.ifmo.se.restaurant.exception.ResourceNotFoundException("Employee not found with id: " + id));
        
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setRole(dto.getRole());
        if (dto.getIsActive() != null) {
            employee.setIsActive(dto.getIsActive());
        }
        
        employee = employeeRepository.save(employee);
        return ResponseEntity.ok(toDto(employee));
    }

    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete employee (soft delete)")
    public ResponseEntity<Void> deleteEmployee(@PathVariable @NonNull Long id) {
        ru.ifmo.se.restaurant.model.entity.Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ru.ifmo.se.restaurant.exception.ResourceNotFoundException("Employee not found with id: " + id));
        
        employee.setIsActive(false);
        employeeRepository.save(employee);
        return ResponseEntity.noContent().build();
    }

    private EmployeeDto toDto(ru.ifmo.se.restaurant.model.entity.Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setRole(employee.getRole());
        dto.setIsActive(employee.getIsActive());
        return dto;
    }
}

