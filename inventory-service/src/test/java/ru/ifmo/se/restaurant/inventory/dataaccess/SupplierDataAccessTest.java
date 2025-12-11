package ru.ifmo.se.restaurant.inventory.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.inventory.entity.Supplier;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.repository.SupplierRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.inventory.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class SupplierDataAccessTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierDataAccess supplierDataAccess;

    @Test
    void findById_WhenExists_ReturnsOptionalWithSupplier() {
        // Given
        Long supplierId = 1L;
        Supplier supplier = createMockSupplier(supplierId);
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

        // When
        Optional<Supplier> result = supplierDataAccess.findById(supplierId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(supplierId);
        verify(supplierRepository).findById(supplierId);
    }

    @Test
    void findById_WhenNotExists_ReturnsEmptyOptional() {
        // Given
        Long supplierId = 999L;
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        // When
        Optional<Supplier> result = supplierDataAccess.findById(supplierId);

        // Then
        assertThat(result).isEmpty();
        verify(supplierRepository).findById(supplierId);
    }

    @Test
    void getById_WhenExists_ReturnsSupplier() {
        // Given
        Long supplierId = 1L;
        Supplier supplier = createMockSupplier(supplierId);
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

        // When
        Supplier result = supplierDataAccess.getById(supplierId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(supplierId);
        verify(supplierRepository).findById(supplierId);
    }

    @Test
    void getById_WhenNotExists_ThrowsException() {
        // Given
        Long supplierId = 999L;
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> supplierDataAccess.getById(supplierId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found");

        verify(supplierRepository).findById(supplierId);
    }

    @Test
    void save_SavesSupplierAndReturns() {
        // Given
        Supplier supplier = createMockSupplier(null);
        Supplier savedSupplier = createMockSupplier(1L);
        when(supplierRepository.save(supplier)).thenReturn(savedSupplier);

        // When
        Supplier result = supplierDataAccess.save(supplier);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(supplierRepository).save(supplier);
    }

    @Test
    void findAll_ReturnsAllSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(
                createMockSupplier(1L),
                createMockSupplier(2L)
        );
        when(supplierRepository.findAll()).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierDataAccess.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(supplierRepository).findAll();
    }

    @Test
    void existsById_WhenExists_ReturnsTrue() {
        // Given
        Long supplierId = 1L;
        when(supplierRepository.existsById(supplierId)).thenReturn(true);

        // When
        boolean result = supplierDataAccess.existsById(supplierId);

        // Then
        assertThat(result).isTrue();
        verify(supplierRepository).existsById(supplierId);
    }

    @Test
    void existsById_WhenNotExists_ReturnsFalse() {
        // Given
        Long supplierId = 999L;
        when(supplierRepository.existsById(supplierId)).thenReturn(false);

        // When
        boolean result = supplierDataAccess.existsById(supplierId);

        // Then
        assertThat(result).isFalse();
        verify(supplierRepository).existsById(supplierId);
    }

    @Test
    void deleteById_DeletesSupplier() {
        // Given
        Long supplierId = 1L;
        doNothing().when(supplierRepository).deleteById(supplierId);

        // When
        supplierDataAccess.deleteById(supplierId);

        // Then
        verify(supplierRepository).deleteById(supplierId);
    }
}
