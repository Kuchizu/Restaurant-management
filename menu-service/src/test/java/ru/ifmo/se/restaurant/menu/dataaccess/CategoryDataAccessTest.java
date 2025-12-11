package ru.ifmo.se.restaurant.menu.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.menu.entity.Category;
import ru.ifmo.se.restaurant.menu.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.repository.CategoryRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.menu.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class CategoryDataAccessTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryDataAccess categoryDataAccess;

    @Test
    void findById_WhenExists_ReturnsOptionalWithCategory() {
        // Given
        Long categoryId = 1L;
        Category category = createMockCategory(categoryId);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // When
        Optional<Category> result = categoryDataAccess.findById(categoryId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(categoryId);
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void findById_WhenNotExists_ReturnsEmptyOptional() {
        // Given
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When
        Optional<Category> result = categoryDataAccess.findById(categoryId);

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void getById_WhenExists_ReturnsCategory() {
        // Given
        Long categoryId = 1L;
        Category category = createMockCategory(categoryId);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // When
        Category result = categoryDataAccess.getById(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void getById_WhenNotExists_ThrowsException() {
        // Given
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryDataAccess.getById(categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id: " + categoryId);

        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void save_SavesCategoryAndReturns() {
        // Given
        Category category = createMockCategory(null);
        Category savedCategory = createMockCategory(1L);
        when(categoryRepository.save(category)).thenReturn(savedCategory);

        // When
        Category result = categoryDataAccess.save(category);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(categoryRepository).save(category);
    }

    @Test
    void findAll_ReturnsAllCategories() {
        // Given
        List<Category> categories = Arrays.asList(
                createMockCategory(1L),
                createMockCategory(2L)
        );
        when(categoryRepository.findAll()).thenReturn(categories);

        // When
        List<Category> result = categoryDataAccess.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(categoryRepository).findAll();
    }

    @Test
    void findByName_WhenExists_ReturnsOptionalWithCategory() {
        // Given
        String categoryName = "Test Category";
        Category category = createMockCategory(1L, categoryName);
        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.of(category));

        // When
        Optional<Category> result = categoryDataAccess.findByName(categoryName);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(categoryName);
        verify(categoryRepository).findByName(categoryName);
    }

    @Test
    void findByIsActive_ReturnsMatchingCategories() {
        // Given
        Boolean isActive = true;
        List<Category> categories = Arrays.asList(
                createMockCategoryWithIsActive(1L, true),
                createMockCategoryWithIsActive(2L, true)
        );
        when(categoryRepository.findByIsActive(isActive)).thenReturn(categories);

        // When
        List<Category> result = categoryDataAccess.findByIsActive(isActive);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> c.getIsActive());
        verify(categoryRepository).findByIsActive(isActive);
    }

    @Test
    void existsById_WhenExists_ReturnsTrue() {
        // Given
        Long categoryId = 1L;
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        // When
        boolean result = categoryDataAccess.existsById(categoryId);

        // Then
        assertThat(result).isTrue();
        verify(categoryRepository).existsById(categoryId);
    }

    @Test
    void existsById_WhenNotExists_ReturnsFalse() {
        // Given
        Long categoryId = 999L;
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        // When
        boolean result = categoryDataAccess.existsById(categoryId);

        // Then
        assertThat(result).isFalse();
        verify(categoryRepository).existsById(categoryId);
    }

    @Test
    void deleteById_DeletesCategory() {
        // Given
        Long categoryId = 1L;
        doNothing().when(categoryRepository).deleteById(categoryId);

        // When
        categoryDataAccess.deleteById(categoryId);

        // Then
        verify(categoryRepository).deleteById(categoryId);
    }
}
