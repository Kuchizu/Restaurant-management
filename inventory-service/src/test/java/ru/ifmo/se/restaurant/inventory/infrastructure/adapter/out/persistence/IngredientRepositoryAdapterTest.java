package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.ifmo.se.restaurant.inventory.domain.entity.Ingredient;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.IngredientJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.IngredientJpaRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientRepositoryAdapterTest {

    @Mock
    private IngredientJpaRepository jpaRepository;

    private IngredientRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new IngredientRepositoryAdapter(jpaRepository);
    }

    private IngredientJpaEntity createEntity(Long id, String name) {
        return IngredientJpaEntity.builder()
                .id(id)
                .name(name)
                .unit("kg")
                .description("Test")
                .build();
    }

    @Test
    void save_ShouldReturnSavedIngredient() {
        IngredientJpaEntity entity = createEntity(1L, "Salt");
        when(jpaRepository.save(any())).thenReturn(entity);

        Ingredient result = adapter.save(Ingredient.builder().name("Salt").build());

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Salt", result.getName());
    }

    @Test
    void findById_ShouldReturnIngredient_WhenExists() {
        IngredientJpaEntity entity = createEntity(1L, "Sugar");
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<Ingredient> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Sugar", result.get().getName());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Ingredient> result = adapter.findById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllIngredients() {
        List<IngredientJpaEntity> entities = List.of(
                createEntity(1L, "Salt"),
                createEntity(2L, "Sugar")
        );
        when(jpaRepository.findAll()).thenReturn(entities);

        List<Ingredient> result = adapter.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findAllPaged_ShouldReturnPagedIngredients() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<IngredientJpaEntity> page = new PageImpl<>(List.of(createEntity(1L, "Salt")));
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        Page<Ingredient> result = adapter.findAll(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findAllSlice_ShouldReturnSlice() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<IngredientJpaEntity> page = new PageImpl<>(List.of(createEntity(1L, "Salt")));
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        var result = adapter.findAllSlice(pageable);

        assertEquals(1, result.getContent().size());
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
