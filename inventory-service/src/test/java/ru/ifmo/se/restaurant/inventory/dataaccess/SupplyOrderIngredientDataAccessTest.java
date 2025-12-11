package ru.ifmo.se.restaurant.inventory.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.inventory.entity.SupplyOrderIngredient;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.repository.SupplyOrderIngredientRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.inventory.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class SupplyOrderIngredientDataAccessTest {

    @Mock
    private SupplyOrderIngredientRepository supplyOrderIngredientRepository;

    @InjectMocks
    private SupplyOrderIngredientDataAccess supplyOrderIngredientDataAccess;

    @Test
    void findById_WhenExists_ReturnsOptionalWithSupplyOrderIngredient() {
        // Given
        Long itemId = 1L;
        SupplyOrderIngredient item = createMockSupplyOrderIngredient(itemId);
        when(supplyOrderIngredientRepository.findById(itemId)).thenReturn(Optional.of(item));

        // When
        Optional<SupplyOrderIngredient> result = supplyOrderIngredientDataAccess.findById(itemId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(itemId);
        verify(supplyOrderIngredientRepository).findById(itemId);
    }

    @Test
    void findById_WhenNotExists_ReturnsEmptyOptional() {
        // Given
        Long itemId = 999L;
        when(supplyOrderIngredientRepository.findById(itemId)).thenReturn(Optional.empty());

        // When
        Optional<SupplyOrderIngredient> result = supplyOrderIngredientDataAccess.findById(itemId);

        // Then
        assertThat(result).isEmpty();
        verify(supplyOrderIngredientRepository).findById(itemId);
    }

    @Test
    void getById_WhenExists_ReturnsSupplyOrderIngredient() {
        // Given
        Long itemId = 1L;
        SupplyOrderIngredient item = createMockSupplyOrderIngredient(itemId);
        when(supplyOrderIngredientRepository.findById(itemId)).thenReturn(Optional.of(item));

        // When
        SupplyOrderIngredient result = supplyOrderIngredientDataAccess.getById(itemId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(itemId);
        verify(supplyOrderIngredientRepository).findById(itemId);
    }

    @Test
    void getById_WhenNotExists_ThrowsException() {
        // Given
        Long itemId = 999L;
        when(supplyOrderIngredientRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> supplyOrderIngredientDataAccess.getById(itemId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supply order ingredient not found");

        verify(supplyOrderIngredientRepository).findById(itemId);
    }

    @Test
    void save_SavesSupplyOrderIngredientAndReturns() {
        // Given
        SupplyOrderIngredient item = createMockSupplyOrderIngredient(null);
        SupplyOrderIngredient savedItem = createMockSupplyOrderIngredient(1L);
        when(supplyOrderIngredientRepository.save(item)).thenReturn(savedItem);

        // When
        SupplyOrderIngredient result = supplyOrderIngredientDataAccess.save(item);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(supplyOrderIngredientRepository).save(item);
    }

    @Test
    void findAll_ReturnsAllSupplyOrderIngredients() {
        // Given
        List<SupplyOrderIngredient> items = Arrays.asList(
                createMockSupplyOrderIngredient(1L),
                createMockSupplyOrderIngredient(2L)
        );
        when(supplyOrderIngredientRepository.findAll()).thenReturn(items);

        // When
        List<SupplyOrderIngredient> result = supplyOrderIngredientDataAccess.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(supplyOrderIngredientRepository).findAll();
    }

    @Test
    void findBySupplyOrderId_ReturnsMatchingItems() {
        // Given
        Long supplyOrderId = 1L;
        List<SupplyOrderIngredient> items = Arrays.asList(
                createMockSupplyOrderIngredient(1L),
                createMockSupplyOrderIngredient(2L)
        );
        when(supplyOrderIngredientRepository.findBySupplyOrderId(supplyOrderId)).thenReturn(items);

        // When
        List<SupplyOrderIngredient> result = supplyOrderIngredientDataAccess.findBySupplyOrderId(supplyOrderId);

        // Then
        assertThat(result).hasSize(2);
        verify(supplyOrderIngredientRepository).findBySupplyOrderId(supplyOrderId);
    }

    @Test
    void existsById_WhenExists_ReturnsTrue() {
        // Given
        Long itemId = 1L;
        when(supplyOrderIngredientRepository.existsById(itemId)).thenReturn(true);

        // When
        boolean result = supplyOrderIngredientDataAccess.existsById(itemId);

        // Then
        assertThat(result).isTrue();
        verify(supplyOrderIngredientRepository).existsById(itemId);
    }

    @Test
    void existsById_WhenNotExists_ReturnsFalse() {
        // Given
        Long itemId = 999L;
        when(supplyOrderIngredientRepository.existsById(itemId)).thenReturn(false);

        // When
        boolean result = supplyOrderIngredientDataAccess.existsById(itemId);

        // Then
        assertThat(result).isFalse();
        verify(supplyOrderIngredientRepository).existsById(itemId);
    }

    @Test
    void deleteById_DeletesSupplyOrderIngredient() {
        // Given
        Long itemId = 1L;
        doNothing().when(supplyOrderIngredientRepository).deleteById(itemId);

        // When
        supplyOrderIngredientDataAccess.deleteById(itemId);

        // Then
        verify(supplyOrderIngredientRepository).deleteById(itemId);
    }
}
