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
import ru.ifmo.se.restaurant.menu.domain.entity.Dish;
import ru.ifmo.se.restaurant.menu.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity.CategoryJpaEntity;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity.DishJpaEntity;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.repository.CategoryJpaRepository;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.repository.DishJpaRepository;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.repository.IngredientJpaRepository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DishRepositoryAdapterTest {

    @Mock
    private DishJpaRepository dishJpaRepository;

    @Mock
    private CategoryJpaRepository categoryJpaRepository;

    @Mock
    private IngredientJpaRepository ingredientJpaRepository;

    private DishRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DishRepositoryAdapter(dishJpaRepository, categoryJpaRepository, ingredientJpaRepository);
    }

    private CategoryJpaEntity createCategoryEntity(Long id, String name) {
        return CategoryJpaEntity.builder()
                .id(id)
                .name(name)
                .isActive(true)
                .build();
    }

    private DishJpaEntity createDishEntity(Long id, String name, CategoryJpaEntity category) {
        return DishJpaEntity.builder()
                .id(id)
                .name(name)
                .price(new BigDecimal("15.99"))
                .category(category)
                .isActive(true)
                .ingredients(new HashSet<>())
                .build();
    }

    @Test
    void save_ShouldReturnSavedDish() {
        CategoryJpaEntity categoryEntity = createCategoryEntity(1L, "Main");
        DishJpaEntity dishEntity = createDishEntity(1L, "Pizza", categoryEntity);

        when(categoryJpaRepository.findById(1L)).thenReturn(Optional.of(categoryEntity));
        when(dishJpaRepository.save(any())).thenReturn(dishEntity);

        Dish dish = Dish.builder()
                .name("Pizza")
                .price(new BigDecimal("15.99"))
                .category(Category.builder().id(1L).name("Main").build())
                .isActive(true)
                .build();

        Dish result = adapter.save(dish);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Pizza", result.getName());
    }

    @Test
    void save_ShouldThrowException_WhenCategoryNotFound() {
        when(categoryJpaRepository.findById(99L)).thenReturn(Optional.empty());

        Dish dish = Dish.builder()
                .name("Pizza")
                .price(new BigDecimal("15.99"))
                .category(Category.builder().id(99L).name("Unknown").build())
                .isActive(true)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> adapter.save(dish));
    }

    @Test
    void findById_ShouldReturnDish_WhenExists() {
        CategoryJpaEntity categoryEntity = createCategoryEntity(1L, "Main");
        DishJpaEntity dishEntity = createDishEntity(1L, "Pizza", categoryEntity);
        when(dishJpaRepository.findById(1L)).thenReturn(Optional.of(dishEntity));

        Optional<Dish> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Pizza", result.get().getName());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        when(dishJpaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Dish> result = adapter.findById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void getById_ShouldReturnDish_WhenExists() {
        CategoryJpaEntity categoryEntity = createCategoryEntity(1L, "Main");
        DishJpaEntity dishEntity = createDishEntity(1L, "Pizza", categoryEntity);
        when(dishJpaRepository.findById(1L)).thenReturn(Optional.of(dishEntity));

        Dish result = adapter.getById(1L);

        assertNotNull(result);
        assertEquals("Pizza", result.getName());
    }

    @Test
    void getById_ShouldThrowException_WhenNotExists() {
        when(dishJpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adapter.getById(99L));
    }

    @Test
    void findAll_ShouldReturnAllDishes() {
        CategoryJpaEntity categoryEntity = createCategoryEntity(1L, "Main");
        List<DishJpaEntity> entities = List.of(createDishEntity(1L, "Pizza", categoryEntity));
        when(dishJpaRepository.findAll()).thenReturn(entities);

        List<Dish> result = adapter.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findAllPaged_ShouldReturnPagedDishes() {
        CategoryJpaEntity categoryEntity = createCategoryEntity(1L, "Main");
        Pageable pageable = PageRequest.of(0, 10);
        Page<DishJpaEntity> page = new PageImpl<>(List.of(createDishEntity(1L, "Pizza", categoryEntity)));
        when(dishJpaRepository.findAll(pageable)).thenReturn(page);

        Page<Dish> result = adapter.findAll(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByName_ShouldReturnDish_WhenExists() {
        CategoryJpaEntity categoryEntity = createCategoryEntity(1L, "Main");
        DishJpaEntity dishEntity = createDishEntity(1L, "Pizza", categoryEntity);
        when(dishJpaRepository.findByName("Pizza")).thenReturn(Optional.of(dishEntity));

        Optional<Dish> result = adapter.findByName("Pizza");

        assertTrue(result.isPresent());
    }

    @Test
    void findByIsActive_ShouldReturnActiveDishes() {
        CategoryJpaEntity categoryEntity = createCategoryEntity(1L, "Main");
        List<DishJpaEntity> entities = List.of(createDishEntity(1L, "Pizza", categoryEntity));
        when(dishJpaRepository.findByIsActive(true)).thenReturn(entities);

        List<Dish> result = adapter.findByIsActive(true);

        assertEquals(1, result.size());
    }

    @Test
    void findByCategoryId_ShouldReturnDishesInCategory() {
        CategoryJpaEntity categoryEntity = createCategoryEntity(1L, "Main");
        List<DishJpaEntity> entities = List.of(createDishEntity(1L, "Pizza", categoryEntity));
        when(dishJpaRepository.findByCategoryId(1L)).thenReturn(entities);

        List<Dish> result = adapter.findByCategoryId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        when(dishJpaRepository.existsById(1L)).thenReturn(true);

        assertTrue(adapter.existsById(1L));
    }

    @Test
    void deleteById_ShouldCallRepository() {
        doNothing().when(dishJpaRepository).deleteById(1L);

        adapter.deleteById(1L);

        verify(dishJpaRepository).deleteById(1L);
    }

    @Test
    void updateImageUrl_ShouldUpdateDish() {
        CategoryJpaEntity categoryEntity = createCategoryEntity(1L, "Main");
        DishJpaEntity dishEntity = createDishEntity(1L, "Pizza", categoryEntity);
        when(dishJpaRepository.findById(1L)).thenReturn(Optional.of(dishEntity));
        when(dishJpaRepository.save(any())).thenReturn(dishEntity);

        adapter.updateImageUrl(1L, "http://example.com/pizza.jpg");

        verify(dishJpaRepository).save(any());
    }
}
