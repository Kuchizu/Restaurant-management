package ru.ifmo.se.restaurant.inventory.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.inventory.entity.SupplyOrder;
import ru.ifmo.se.restaurant.inventory.entity.SupplyOrderStatus;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.repository.SupplyOrderRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.inventory.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class SupplyOrderDataAccessTest {

    @Mock
    private SupplyOrderRepository supplyOrderRepository;

    @InjectMocks
    private SupplyOrderDataAccess supplyOrderDataAccess;

    @Test
    void findById_WhenExists_ReturnsOptionalWithSupplyOrder() {
        // Given
        Long orderId = 1L;
        SupplyOrder supplyOrder = createMockSupplyOrder(orderId);
        when(supplyOrderRepository.findById(orderId)).thenReturn(Optional.of(supplyOrder));

        // When
        Optional<SupplyOrder> result = supplyOrderDataAccess.findById(orderId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(orderId);
        verify(supplyOrderRepository).findById(orderId);
    }

    @Test
    void findById_WhenNotExists_ReturnsEmptyOptional() {
        // Given
        Long orderId = 999L;
        when(supplyOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When
        Optional<SupplyOrder> result = supplyOrderDataAccess.findById(orderId);

        // Then
        assertThat(result).isEmpty();
        verify(supplyOrderRepository).findById(orderId);
    }

    @Test
    void getById_WhenExists_ReturnsSupplyOrder() {
        // Given
        Long orderId = 1L;
        SupplyOrder supplyOrder = createMockSupplyOrder(orderId);
        when(supplyOrderRepository.findById(orderId)).thenReturn(Optional.of(supplyOrder));

        // When
        SupplyOrder result = supplyOrderDataAccess.getById(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        verify(supplyOrderRepository).findById(orderId);
    }

    @Test
    void getById_WhenNotExists_ThrowsException() {
        // Given
        Long orderId = 999L;
        when(supplyOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> supplyOrderDataAccess.getById(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supply order not found");

        verify(supplyOrderRepository).findById(orderId);
    }

    @Test
    void save_SavesSupplyOrderAndReturns() {
        // Given
        SupplyOrder supplyOrder = createMockSupplyOrder(null);
        SupplyOrder savedSupplyOrder = createMockSupplyOrder(1L);
        when(supplyOrderRepository.save(supplyOrder)).thenReturn(savedSupplyOrder);

        // When
        SupplyOrder result = supplyOrderDataAccess.save(supplyOrder);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(supplyOrderRepository).save(supplyOrder);
    }

    @Test
    void findAll_ReturnsAllSupplyOrders() {
        // Given
        List<SupplyOrder> supplyOrders = Arrays.asList(
                createMockSupplyOrder(1L),
                createMockSupplyOrder(2L)
        );
        when(supplyOrderRepository.findAll()).thenReturn(supplyOrders);

        // When
        List<SupplyOrder> result = supplyOrderDataAccess.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(supplyOrderRepository).findAll();
    }

    @Test
    void findByStatus_ReturnsMatchingSupplyOrders() {
        // Given
        SupplyOrderStatus status = SupplyOrderStatus.PENDING;
        List<SupplyOrder> supplyOrders = Arrays.asList(
                createMockSupplyOrder(1L),
                createMockSupplyOrder(2L)
        );
        when(supplyOrderRepository.findByStatus(status)).thenReturn(supplyOrders);

        // When
        List<SupplyOrder> result = supplyOrderDataAccess.findByStatus(status);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(o -> o.getStatus() == SupplyOrderStatus.PENDING);
        verify(supplyOrderRepository).findByStatus(status);
    }

    @Test
    void findBySupplierId_ReturnsMatchingSupplyOrders() {
        // Given
        Long supplierId = 1L;
        List<SupplyOrder> supplyOrders = Arrays.asList(
                createMockSupplyOrder(1L),
                createMockSupplyOrder(2L)
        );
        when(supplyOrderRepository.findBySupplierId(supplierId)).thenReturn(supplyOrders);

        // When
        List<SupplyOrder> result = supplyOrderDataAccess.findBySupplierId(supplierId);

        // Then
        assertThat(result).hasSize(2);
        verify(supplyOrderRepository).findBySupplierId(supplierId);
    }

    @Test
    void existsById_WhenExists_ReturnsTrue() {
        // Given
        Long orderId = 1L;
        when(supplyOrderRepository.existsById(orderId)).thenReturn(true);

        // When
        boolean result = supplyOrderDataAccess.existsById(orderId);

        // Then
        assertThat(result).isTrue();
        verify(supplyOrderRepository).existsById(orderId);
    }

    @Test
    void existsById_WhenNotExists_ReturnsFalse() {
        // Given
        Long orderId = 999L;
        when(supplyOrderRepository.existsById(orderId)).thenReturn(false);

        // When
        boolean result = supplyOrderDataAccess.existsById(orderId);

        // Then
        assertThat(result).isFalse();
        verify(supplyOrderRepository).existsById(orderId);
    }

    @Test
    void deleteById_DeletesSupplyOrder() {
        // Given
        Long orderId = 1L;
        doNothing().when(supplyOrderRepository).deleteById(orderId);

        // When
        supplyOrderDataAccess.deleteById(orderId);

        // Then
        verify(supplyOrderRepository).deleteById(orderId);
    }
}
