package ru.ifmo.se.restaurant.order.dataaccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.ifmo.se.restaurant.order.TestDataFactory;
import ru.ifmo.se.restaurant.order.entity.Employee;
import ru.ifmo.se.restaurant.order.entity.EmployeeRole;
import ru.ifmo.se.restaurant.order.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.repository.EmployeeRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeDataAccessTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeDataAccess employeeDataAccess;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = TestDataFactory.createDefaultWaiter();
    }

    @Test
    void findById_ShouldReturnEmployee_WhenExists() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Mono.just(testEmployee));

        // Act & Assert
        StepVerifier.create(employeeDataAccess.findById(1L))
            .expectNext(testEmployee)
            .verifyComplete();

        verify(employeeRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(employeeDataAccess.findById(1L))
            .verifyComplete();

        verify(employeeRepository).findById(1L);
    }

    @Test
    void getById_ShouldReturnEmployee_WhenExists() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Mono.just(testEmployee));

        // Act & Assert
        StepVerifier.create(employeeDataAccess.getById(1L))
            .expectNext(testEmployee)
            .verifyComplete();

        verify(employeeRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(employeeDataAccess.getById(1L))
            .expectError(ResourceNotFoundException.class)
            .verify();

        verify(employeeRepository).findById(1L);
    }

    @Test
    void findAll_ShouldReturnAllEmployees() {
        // Arrange
        Employee chef = TestDataFactory.createEmployee(2L, "Jane", "Smith", EmployeeRole.CHEF);
        when(employeeRepository.findAll()).thenReturn(Flux.just(testEmployee, chef));

        // Act & Assert
        StepVerifier.create(employeeDataAccess.findAll())
            .expectNext(testEmployee)
            .expectNext(chef)
            .verifyComplete();

        verify(employeeRepository).findAll();
    }

    @Test
    void save_ShouldPersistEmployee() {
        // Arrange
        when(employeeRepository.save(any(Employee.class))).thenReturn(Mono.just(testEmployee));

        // Act & Assert
        StepVerifier.create(employeeDataAccess.save(testEmployee))
            .expectNext(testEmployee)
            .verifyComplete();

        verify(employeeRepository).save(testEmployee);
    }

    @Test
    void save_ShouldUpdateEmployee() {
        // Arrange
        testEmployee.setFirstName("UpdatedName");
        when(employeeRepository.save(any(Employee.class))).thenReturn(Mono.just(testEmployee));

        // Act & Assert
        StepVerifier.create(employeeDataAccess.save(testEmployee))
            .expectNextMatches(emp -> emp.getFirstName().equals("UpdatedName"))
            .verifyComplete();

        verify(employeeRepository).save(testEmployee);
    }

    @Test
    void deleteById_ShouldDeleteEmployee() {
        // Arrange
        when(employeeRepository.deleteById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(employeeDataAccess.deleteById(1L))
            .verifyComplete();

        verify(employeeRepository).deleteById(1L);
    }
}
