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
import ru.ifmo.se.restaurant.order.entity.RestaurantTable;
import ru.ifmo.se.restaurant.order.entity.TableStatus;
import ru.ifmo.se.restaurant.order.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.repository.TableRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableDataAccessTest {

    @Mock
    private TableRepository tableRepository;

    @InjectMocks
    private TableDataAccess tableDataAccess;

    private RestaurantTable testTable;

    @BeforeEach
    void setUp() {
        testTable = TestDataFactory.createDefaultTable();
    }

    @Test
    void findById_ShouldReturnTable_WhenExists() {
        // Arrange
        when(tableRepository.findById(anyLong())).thenReturn(Mono.just(testTable));

        // Act & Assert
        StepVerifier.create(tableDataAccess.findById(1L))
            .expectNext(testTable)
            .verifyComplete();

        verify(tableRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // Arrange
        when(tableRepository.findById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(tableDataAccess.findById(1L))
            .verifyComplete();

        verify(tableRepository).findById(1L);
    }

    @Test
    void getById_ShouldReturnTable_WhenExists() {
        // Arrange
        when(tableRepository.findById(anyLong())).thenReturn(Mono.just(testTable));

        // Act & Assert
        StepVerifier.create(tableDataAccess.getById(1L))
            .expectNext(testTable)
            .verifyComplete();

        verify(tableRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(tableRepository.findById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(tableDataAccess.getById(1L))
            .expectError(ResourceNotFoundException.class)
            .verify();

        verify(tableRepository).findById(1L);
    }

    @Test
    void findAll_ShouldReturnAllTables() {
        // Arrange
        RestaurantTable table2 = TestDataFactory.createTable(2L, "T-02", 6, TableStatus.FREE);
        when(tableRepository.findAll()).thenReturn(Flux.just(testTable, table2));

        // Act & Assert
        StepVerifier.create(tableDataAccess.findAll())
            .expectNext(testTable)
            .expectNext(table2)
            .verifyComplete();

        verify(tableRepository).findAll();
    }

    @Test
    void save_ShouldPersistTable() {
        // Arrange
        when(tableRepository.save(any(RestaurantTable.class))).thenReturn(Mono.just(testTable));

        // Act & Assert
        StepVerifier.create(tableDataAccess.save(testTable))
            .expectNext(testTable)
            .verifyComplete();

        verify(tableRepository).save(testTable);
    }

    @Test
    void save_ShouldUpdateTableStatus() {
        // Arrange
        testTable.setStatus(TableStatus.OCCUPIED);
        when(tableRepository.save(any(RestaurantTable.class))).thenReturn(Mono.just(testTable));

        // Act & Assert
        StepVerifier.create(tableDataAccess.save(testTable))
            .expectNextMatches(table -> table.getStatus() == TableStatus.OCCUPIED)
            .verifyComplete();

        verify(tableRepository).save(testTable);
    }

    @Test
    void deleteById_ShouldDeleteTable() {
        // Arrange
        when(tableRepository.deleteById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(tableDataAccess.deleteById(1L))
            .verifyComplete();

        verify(tableRepository).deleteById(1L);
    }
}
