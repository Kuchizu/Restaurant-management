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
import ru.ifmo.se.restaurant.inventory.domain.entity.Inventory;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.IngredientJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.InventoryJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.IngredientJpaRepository;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.InventoryJpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryRepositoryAdapterTest {

    @Mock
    private InventoryJpaRepository jpaRepository;

    @Mock
    private IngredientJpaRepository ingredientJpaRepository;

    private InventoryRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InventoryRepositoryAdapter(jpaRepository, ingredientJpaRepository);
    }

    private IngredientJpaEntity createIngredientEntity(Long id, String name) {
        return IngredientJpaEntity.builder()
                .id(id)
                .name(name)
                .unit("kg")
                .build();
    }

    private InventoryJpaEntity createInventoryEntity(Long id, IngredientJpaEntity ingredient) {
        return InventoryJpaEntity.builder()
                .id(id)
                .ingredient(ingredient)
                .quantity(new BigDecimal("100"))
                .minQuantity(new BigDecimal("10"))
                .maxQuantity(new BigDecimal("500"))
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    void save_ShouldReturnSavedInventory() {
        IngredientJpaEntity ingredientEntity = createIngredientEntity(1L, "Salt");
        InventoryJpaEntity inventoryEntity = createInventoryEntity(1L, ingredientEntity);

        when(ingredientJpaRepository.findById(1L)).thenReturn(Optional.of(ingredientEntity));
        when(jpaRepository.save(any())).thenReturn(inventoryEntity);

        Inventory inventory = Inventory.builder()
                .ingredient(Ingredient.builder().id(1L).name("Salt").build())
                .quantity(new BigDecimal("100"))
                .minQuantity(new BigDecimal("10"))
                .maxQuantity(new BigDecimal("500"))
                .build();

        Inventory result = adapter.save(inventory);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void save_ShouldThrowException_WhenIngredientNotFound() {
        when(ingredientJpaRepository.findById(99L)).thenReturn(Optional.empty());

        Inventory inventory = Inventory.builder()
                .ingredient(Ingredient.builder().id(99L).name("Unknown").build())
                .quantity(new BigDecimal("100"))
                .build();

        assertThrows(IllegalArgumentException.class, () -> adapter.save(inventory));
    }

    @Test
    void findById_ShouldReturnInventory_WhenExists() {
        IngredientJpaEntity ingredientEntity = createIngredientEntity(1L, "Salt");
        InventoryJpaEntity inventoryEntity = createInventoryEntity(1L, ingredientEntity);
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(inventoryEntity));

        Optional<Inventory> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Inventory> result = adapter.findById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllInventoryItems() {
        IngredientJpaEntity ingredientEntity = createIngredientEntity(1L, "Salt");
        List<InventoryJpaEntity> entities = List.of(createInventoryEntity(1L, ingredientEntity));
        when(jpaRepository.findAll()).thenReturn(entities);

        List<Inventory> result = adapter.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findAllPaged_ShouldReturnPagedInventory() {
        IngredientJpaEntity ingredientEntity = createIngredientEntity(1L, "Salt");
        Pageable pageable = PageRequest.of(0, 10);
        Page<InventoryJpaEntity> page = new PageImpl<>(List.of(createInventoryEntity(1L, ingredientEntity)));
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        Page<Inventory> result = adapter.findAll(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findAllSlice_ShouldReturnSlice() {
        IngredientJpaEntity ingredientEntity = createIngredientEntity(1L, "Salt");
        Pageable pageable = PageRequest.of(0, 10);
        Page<InventoryJpaEntity> page = new PageImpl<>(List.of(createInventoryEntity(1L, ingredientEntity)));
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        var result = adapter.findAllSlice(pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void findByIngredientId_ShouldReturnInventory_WhenExists() {
        IngredientJpaEntity ingredientEntity = createIngredientEntity(1L, "Salt");
        InventoryJpaEntity inventoryEntity = createInventoryEntity(1L, ingredientEntity);
        when(jpaRepository.findByIngredientId(1L)).thenReturn(Optional.of(inventoryEntity));

        Optional<Inventory> result = adapter.findByIngredientId(1L);

        assertTrue(result.isPresent());
    }

    @Test
    void findLowStockItems_ShouldReturnLowStockItems() {
        IngredientJpaEntity ingredientEntity = createIngredientEntity(1L, "Salt");
        List<InventoryJpaEntity> entities = List.of(createInventoryEntity(1L, ingredientEntity));
        when(jpaRepository.findLowStockItems()).thenReturn(entities);

        List<Inventory> result = adapter.findLowStockItems();

        assertEquals(1, result.size());
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        when(jpaRepository.existsById(1L)).thenReturn(true);

        assertTrue(adapter.existsById(1L));
    }

    @Test
    void deleteById_ShouldCallRepository() {
        doNothing().when(jpaRepository).deleteById(1L);

        adapter.deleteById(1L);

        verify(jpaRepository).deleteById(1L);
    }
}
