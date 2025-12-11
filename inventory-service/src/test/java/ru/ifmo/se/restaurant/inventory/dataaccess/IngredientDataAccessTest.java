package ru.ifmo.se.restaurant.inventory.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.inventory.entity.Ingredient;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.repository.IngredientRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.inventory.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class IngredientDataAccessTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private IngredientDataAccess ingredientDataAccess;

    @Test
    void findById_WhenExists_ReturnsOptionalWithIngredient() {
        // Given
        Long ingredientId = 1L;
        Ingredient ingredient = createMockIngredient(ingredientId);
        when(ingredientRepository.findById(ingredientId)).thenReturn(Optional.of(ingredient));

        // When
        Optional<Ingredient> result = ingredientDataAccess.findById(ingredientId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(ingredientId);
        verify(ingredientRepository).findById(ingredientId);
    }

    @Test
    void findById_WhenNotExists_ReturnsEmptyOptional() {
        // Given
        Long ingredientId = 999L;
        when(ingredientRepository.findById(ingredientId)).thenReturn(Optional.empty());

        // When
        Optional<Ingredient> result = ingredientDataAccess.findById(ingredientId);

        // Then
        assertThat(result).isEmpty();
        verify(ingredientRepository).findById(ingredientId);
    }

    @Test
    void getById_WhenExists_ReturnsIngredient() {
        // Given
        Long ingredientId = 1L;
        Ingredient ingredient = createMockIngredient(ingredientId);
        when(ingredientRepository.findById(ingredientId)).thenReturn(Optional.of(ingredient));

        // When
        Ingredient result = ingredientDataAccess.getById(ingredientId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ingredientId);
        verify(ingredientRepository).findById(ingredientId);
    }

    @Test
    void getById_WhenNotExists_ThrowsException() {
        // Given
        Long ingredientId = 999L;
        when(ingredientRepository.findById(ingredientId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ingredientDataAccess.getById(ingredientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ingredient not found");

        verify(ingredientRepository).findById(ingredientId);
    }

    @Test
    void save_SavesIngredientAndReturns() {
        // Given
        Ingredient ingredient = createMockIngredient(null);
        Ingredient savedIngredient = createMockIngredient(1L);
        when(ingredientRepository.save(ingredient)).thenReturn(savedIngredient);

        // When
        Ingredient result = ingredientDataAccess.save(ingredient);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(ingredientRepository).save(ingredient);
    }

    @Test
    void findAll_ReturnsAllIngredients() {
        // Given
        List<Ingredient> ingredients = Arrays.asList(
                createMockIngredient(1L),
                createMockIngredient(2L)
        );
        when(ingredientRepository.findAll()).thenReturn(ingredients);

        // When
        List<Ingredient> result = ingredientDataAccess.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(ingredientRepository).findAll();
    }

    @Test
    void findByName_WhenExists_ReturnsOptionalWithIngredient() {
        // Given
        String name = "Test Ingredient 1";
        Ingredient ingredient = createMockIngredient(1L);
        when(ingredientRepository.findByName(name)).thenReturn(Optional.of(ingredient));

        // When
        Optional<Ingredient> result = ingredientDataAccess.findByName(name);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(name);
        verify(ingredientRepository).findByName(name);
    }

    @Test
    void existsById_WhenExists_ReturnsTrue() {
        // Given
        Long ingredientId = 1L;
        when(ingredientRepository.existsById(ingredientId)).thenReturn(true);

        // When
        boolean result = ingredientDataAccess.existsById(ingredientId);

        // Then
        assertThat(result).isTrue();
        verify(ingredientRepository).existsById(ingredientId);
    }

    @Test
    void existsById_WhenNotExists_ReturnsFalse() {
        // Given
        Long ingredientId = 999L;
        when(ingredientRepository.existsById(ingredientId)).thenReturn(false);

        // When
        boolean result = ingredientDataAccess.existsById(ingredientId);

        // Then
        assertThat(result).isFalse();
        verify(ingredientRepository).existsById(ingredientId);
    }

    @Test
    void deleteById_DeletesIngredient() {
        // Given
        Long ingredientId = 1L;
        doNothing().when(ingredientRepository).deleteById(ingredientId);

        // When
        ingredientDataAccess.deleteById(ingredientId);

        // Then
        verify(ingredientRepository).deleteById(ingredientId);
    }
}
