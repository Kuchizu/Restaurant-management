package ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.ifmo.se.restaurant.menu.domain.entity.Category;
import ru.ifmo.se.restaurant.menu.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity.CategoryJpaEntity;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.repository.CategoryJpaRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryAdapterTest {

    @Mock
    private CategoryJpaRepository jpaRepository;

    private CategoryRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CategoryRepositoryAdapter(jpaRepository);
    }

    private CategoryJpaEntity createEntity(Long id, String name) {
        return CategoryJpaEntity.builder()
                .id(id)
                .name(name)
                .description("Test description")
                .isActive(true)
                .build();
    }

    @Test
    void save_ShouldReturnSavedCategory() {
        CategoryJpaEntity entity = createEntity(1L, "Appetizers");
        when(jpaRepository.save(any())).thenReturn(entity);

        Category result = adapter.save(Category.builder().name("Appetizers").isActive(true).build());

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Appetizers", result.getName());
        verify(jpaRepository).save(any());
    }

    @Test
    void findById_ShouldReturnCategory_WhenExists() {
        CategoryJpaEntity entity = createEntity(1L, "Main Course");
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<Category> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Main Course", result.get().getName());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Category> result = adapter.findById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void getById_ShouldReturnCategory_WhenExists() {
        CategoryJpaEntity entity = createEntity(1L, "Desserts");
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        Category result = adapter.getById(1L);

        assertNotNull(result);
        assertEquals("Desserts", result.getName());
    }

    @Test
    void getById_ShouldThrowException_WhenNotExists() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adapter.getById(99L));
    }

    @Test
    void findAll_ShouldReturnAllCategories() {
        List<CategoryJpaEntity> entities = List.of(
                createEntity(1L, "Cat1"),
                createEntity(2L, "Cat2")
        );
        when(jpaRepository.findAll()).thenReturn(entities);

        List<Category> result = adapter.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findAllPaged_ShouldReturnPagedCategories() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryJpaEntity> page = new PageImpl<>(List.of(createEntity(1L, "Cat1")));
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        Page<Category> result = adapter.findAll(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findAllSlice_ShouldReturnSlice() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryJpaEntity> page = new PageImpl<>(List.of(createEntity(1L, "Cat1")));
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        var result = adapter.findAllSlice(pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void findByName_ShouldReturnCategory_WhenExists() {
        CategoryJpaEntity entity = createEntity(1L, "Beverages");
        when(jpaRepository.findByName("Beverages")).thenReturn(Optional.of(entity));

        Optional<Category> result = adapter.findByName("Beverages");

        assertTrue(result.isPresent());
        assertEquals("Beverages", result.get().getName());
    }

    @Test
    void findByIsActive_ShouldReturnActiveCategories() {
        List<CategoryJpaEntity> entities = List.of(createEntity(1L, "Active"));
        when(jpaRepository.findByIsActive(true)).thenReturn(entities);

        List<Category> result = adapter.findByIsActive(true);

        assertEquals(1, result.size());
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        when(jpaRepository.existsById(1L)).thenReturn(true);

        assertTrue(adapter.existsById(1L));
    }

    @Test
    void existsById_ShouldReturnFalse_WhenNotExists() {
        when(jpaRepository.existsById(99L)).thenReturn(false);

        assertFalse(adapter.existsById(99L));
    }

    @Test
    void deleteById_ShouldCallRepository() {
        doNothing().when(jpaRepository).deleteById(1L);

        adapter.deleteById(1L);

        verify(jpaRepository).deleteById(1L);
    }
}
