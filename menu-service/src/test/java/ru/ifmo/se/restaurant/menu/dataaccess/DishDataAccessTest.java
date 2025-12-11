package ru.ifmo.se.restaurant.menu.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.ifmo.se.restaurant.menu.entity.Dish;
import ru.ifmo.se.restaurant.menu.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.repository.DishRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.menu.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class DishDataAccessTest {

    @Mock
    private DishRepository dishRepository;

    @InjectMocks
    private DishDataAccess dishDataAccess;

    @Test
    void findById_WhenExists_ReturnsOptionalWithDish() {
        // Given
        Long dishId = 1L;
        Dish dish = createMockDish(dishId);
        when(dishRepository.findById(dishId)).thenReturn(Optional.of(dish));

        // When
        Optional<Dish> result = dishDataAccess.findById(dishId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(dishId);
        verify(dishRepository).findById(dishId);
    }

    @Test
    void findById_WhenNotExists_ReturnsEmptyOptional() {
        // Given
        Long dishId = 999L;
        when(dishRepository.findById(dishId)).thenReturn(Optional.empty());

        // When
        Optional<Dish> result = dishDataAccess.findById(dishId);

        // Then
        assertThat(result).isEmpty();
        verify(dishRepository).findById(dishId);
    }

    @Test
    void getById_WhenExists_ReturnsDish() {
        // Given
        Long dishId = 1L;
        Dish dish = createMockDish(dishId);
        when(dishRepository.findById(dishId)).thenReturn(Optional.of(dish));

        // When
        Dish result = dishDataAccess.getById(dishId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(dishId);
        verify(dishRepository).findById(dishId);
    }

    @Test
    void getById_WhenNotExists_ThrowsException() {
        // Given
        Long dishId = 999L;
        when(dishRepository.findById(dishId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dishDataAccess.getById(dishId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Dish not found with id: " + dishId);

        verify(dishRepository).findById(dishId);
    }

    @Test
    void save_SavesDishAndReturns() {
        // Given
        Dish dish = createMockDish(null);
        Dish savedDish = createMockDish(1L);
        when(dishRepository.save(dish)).thenReturn(savedDish);

        // When
        Dish result = dishDataAccess.save(dish);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(dishRepository).save(dish);
    }

    @Test
    void findAll_ReturnsAllDishes() {
        // Given
        List<Dish> dishes = Arrays.asList(
                createMockDish(1L),
                createMockDish(2L)
        );
        when(dishRepository.findAll()).thenReturn(dishes);

        // When
        List<Dish> result = dishDataAccess.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(dishRepository).findAll();
    }

    @Test
    void findAllWithPageable_ReturnsPagedDishes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Dish> dishes = Arrays.asList(
                createMockDish(1L),
                createMockDish(2L)
        );
        Page<Dish> dishPage = new PageImpl<>(dishes, pageable, dishes.size());
        when(dishRepository.findAll(pageable)).thenReturn(dishPage);

        // When
        Page<Dish> result = dishDataAccess.findAll(pageable);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(dishRepository).findAll(pageable);
    }

    @Test
    void findByName_WhenExists_ReturnsOptionalWithDish() {
        // Given
        String dishName = "Test Dish";
        Dish dish = createMockDish(1L);
        dish.setName(dishName);
        when(dishRepository.findByName(dishName)).thenReturn(Optional.of(dish));

        // When
        Optional<Dish> result = dishDataAccess.findByName(dishName);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(dishName);
        verify(dishRepository).findByName(dishName);
    }

    @Test
    void findByIsActive_ReturnsMatchingDishes() {
        // Given
        Boolean isActive = true;
        List<Dish> dishes = Arrays.asList(
                createMockDishWithIsActive(1L, true),
                createMockDishWithIsActive(2L, true)
        );
        when(dishRepository.findByIsActive(isActive)).thenReturn(dishes);

        // When
        List<Dish> result = dishDataAccess.findByIsActive(isActive);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Dish::getIsActive);
        verify(dishRepository).findByIsActive(isActive);
    }

    @Test
    void findByCategoryId_ReturnsMatchingDishes() {
        // Given
        Long categoryId = 1L;
        List<Dish> dishes = Arrays.asList(
                createMockDish(1L),
                createMockDish(2L)
        );
        when(dishRepository.findByCategoryId(categoryId)).thenReturn(dishes);

        // When
        List<Dish> result = dishDataAccess.findByCategoryId(categoryId);

        // Then
        assertThat(result).hasSize(2);
        verify(dishRepository).findByCategoryId(categoryId);
    }

    @Test
    void findActiveDishById_WhenExists_ReturnsOptionalWithDish() {
        // Given
        Long dishId = 1L;
        Dish dish = createMockDishWithIsActive(dishId, true);
        when(dishRepository.findActiveDishById(dishId)).thenReturn(Optional.of(dish));

        // When
        Optional<Dish> result = dishDataAccess.findActiveDishById(dishId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(dishId);
        assertThat(result.get().getIsActive()).isTrue();
        verify(dishRepository).findActiveDishById(dishId);
    }

    @Test
    void existsById_WhenExists_ReturnsTrue() {
        // Given
        Long dishId = 1L;
        when(dishRepository.existsById(dishId)).thenReturn(true);

        // When
        boolean result = dishDataAccess.existsById(dishId);

        // Then
        assertThat(result).isTrue();
        verify(dishRepository).existsById(dishId);
    }

    @Test
    void existsById_WhenNotExists_ReturnsFalse() {
        // Given
        Long dishId = 999L;
        when(dishRepository.existsById(dishId)).thenReturn(false);

        // When
        boolean result = dishDataAccess.existsById(dishId);

        // Then
        assertThat(result).isFalse();
        verify(dishRepository).existsById(dishId);
    }

    @Test
    void deleteById_DeletesDish() {
        // Given
        Long dishId = 1L;
        doNothing().when(dishRepository).deleteById(dishId);

        // When
        dishDataAccess.deleteById(dishId);

        // Then
        verify(dishRepository).deleteById(dishId);
    }
}
