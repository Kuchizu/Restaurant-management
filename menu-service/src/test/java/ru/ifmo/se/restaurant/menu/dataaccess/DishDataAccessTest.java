package ru.ifmo.se.restaurant.menu.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.ifmo.se.restaurant.menu.entity.Dish;
import ru.ifmo.se.restaurant.menu.repository.DishRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DishDataAccessTest {

    @Mock
    private DishRepository repository;

    @InjectMocks
    private DishDataAccess dataAccess;

    @Test
    void save() {
        Dish dish = new Dish();
        when(repository.save(any())).thenReturn(dish);
        assertNotNull(dataAccess.save(dish));
    }

    @Test
    void findById() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Dish()));
        assertTrue(dataAccess.findById(1L).isPresent());
    }

    @Test
    void getById() {
        Dish dish = new Dish();
        when(repository.findById(1L)).thenReturn(Optional.of(dish));
        assertNotNull(dataAccess.getById(1L));
    }

    @Test
    void findAll() {
        when(repository.findAll()).thenReturn(Collections.emptyList());
        assertNotNull(dataAccess.findAll());
    }

    @Test
    void findAllPaginated() {
        Page<Dish> page = new PageImpl<>(Collections.emptyList());
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);
        assertNotNull(dataAccess.findAll(PageRequest.of(0, 20)));
    }

    @Test
    void findAllSlice() {
        Page<Dish> page = new PageImpl<>(Collections.emptyList());
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);
        assertNotNull(dataAccess.findAllSlice(PageRequest.of(0, 20)));
    }

    @Test
    void findByCategoryId() {
        when(repository.findByCategoryId(1L)).thenReturn(Collections.emptyList());
        assertNotNull(dataAccess.findByCategoryId(1L));
    }

    @Test
    void findByIsActive() {
        when(repository.findByIsActive(true)).thenReturn(Collections.emptyList());
        assertNotNull(dataAccess.findByIsActive(true));
    }

    @Test
    void findByName() {
        when(repository.findByName("Test")).thenReturn(Optional.of(new Dish()));
        assertTrue(dataAccess.findByName("Test").isPresent());
    }

    @Test
    void findActiveDishById() {
        when(repository.findActiveDishById(1L)).thenReturn(Optional.of(new Dish()));
        assertTrue(dataAccess.findActiveDishById(1L).isPresent());
    }

    @Test
    void existsById() {
        when(repository.existsById(1L)).thenReturn(true);
        assertTrue(dataAccess.existsById(1L));
    }

    @Test
    void deleteById() {
        doNothing().when(repository).deleteById(1L);
        dataAccess.deleteById(1L);
        verify(repository).deleteById(1L);
    }
}
