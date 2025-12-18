package ru.ifmo.se.restaurant.inventory.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.inventory.entity.Inventory;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.repository.InventoryRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.inventory.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class InventoryDataAccessTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryDataAccess inventoryDataAccess;

    @Test
    void findById_WhenExists_ReturnsOptionalWithInventory() {
        // Given
        Long inventoryId = 1L;
        Inventory inventory = createMockInventory(inventoryId);
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));

        // When
        Optional<Inventory> result = inventoryDataAccess.findById(inventoryId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(inventoryId);
        verify(inventoryRepository).findById(inventoryId);
    }

    @Test
    void findById_WhenNotExists_ReturnsEmptyOptional() {
        // Given
        Long inventoryId = 999L;
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        // When
        Optional<Inventory> result = inventoryDataAccess.findById(inventoryId);

        // Then
        assertThat(result).isEmpty();
        verify(inventoryRepository).findById(inventoryId);
    }

    @Test
    void getById_WhenExists_ReturnsInventory() {
        // Given
        Long inventoryId = 1L;
        Inventory inventory = createMockInventory(inventoryId);
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));

        // When
        Inventory result = inventoryDataAccess.getById(inventoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(inventoryId);
        verify(inventoryRepository).findById(inventoryId);
    }

    @Test
    void getById_WhenNotExists_ThrowsException() {
        // Given
        Long inventoryId = 999L;
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryDataAccess.getById(inventoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Inventory not found");

        verify(inventoryRepository).findById(inventoryId);
    }

    @Test
    void save_SavesInventoryAndReturns() {
        // Given
        Inventory inventory = createMockInventory(null);
        Inventory savedInventory = createMockInventory(1L);
        when(inventoryRepository.save(inventory)).thenReturn(savedInventory);

        // When
        Inventory result = inventoryDataAccess.save(inventory);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void findAll_ReturnsAllInventory() {
        // Given
        List<Inventory> inventories = Arrays.asList(
                createMockInventory(1L),
                createMockInventory(2L)
        );
        when(inventoryRepository.findAll()).thenReturn(inventories);

        // When
        List<Inventory> result = inventoryDataAccess.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(inventoryRepository).findAll();
    }

    @Test
    void findByIngredientId_WhenExists_ReturnsOptionalWithInventory() {
        // Given
        Long ingredientId = 1L;
        Inventory inventory = createMockInventory(1L);
        when(inventoryRepository.findByIngredientId(ingredientId)).thenReturn(Optional.of(inventory));

        // When
        Optional<Inventory> result = inventoryDataAccess.findByIngredientId(ingredientId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getIngredient().getId()).isEqualTo(ingredientId);
        verify(inventoryRepository).findByIngredientId(ingredientId);
    }

    @Test
    void findLowStockItems_ReturnsLowStockInventory() {
        // Given
        List<Inventory> lowStockItems = Arrays.asList(
                createLowStockInventory(1L, createMockIngredient(1L)),
                createLowStockInventory(2L, createMockIngredient(2L))
        );
        when(inventoryRepository.findLowStockItems()).thenReturn(lowStockItems);

        // When
        List<Inventory> result = inventoryDataAccess.findLowStockItems();

        // Then
        assertThat(result).hasSize(2);
        verify(inventoryRepository).findLowStockItems();
    }

    @Test
    void existsById_WhenExists_ReturnsTrue() {
        // Given
        Long inventoryId = 1L;
        when(inventoryRepository.existsById(inventoryId)).thenReturn(true);

        // When
        boolean result = inventoryDataAccess.existsById(inventoryId);

        // Then
        assertThat(result).isTrue();
        verify(inventoryRepository).existsById(inventoryId);
    }

    @Test
    void existsById_WhenNotExists_ReturnsFalse() {
        // Given
        Long inventoryId = 999L;
        when(inventoryRepository.existsById(inventoryId)).thenReturn(false);

        // When
        boolean result = inventoryDataAccess.existsById(inventoryId);

        // Then
        assertThat(result).isFalse();
        verify(inventoryRepository).existsById(inventoryId);
    }

    @Test
    void deleteById_DeletesInventory() {
        // Given
        Long inventoryId = 1L;
        doNothing().when(inventoryRepository).deleteById(inventoryId);

        // When
        inventoryDataAccess.deleteById(inventoryId);

        // Then
        verify(inventoryRepository).deleteById(inventoryId);
    }
}
