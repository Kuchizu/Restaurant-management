package ru.ifmo.se.restaurant.menu.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.menu.entity.Ingredient;
import ru.ifmo.se.restaurant.menu.repository.IngredientRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientDataAccessTest {

    @Mock
    private IngredientRepository repository;

    @InjectMocks
    private IngredientDataAccess dataAccess;

    @Test
    void save() {
        Ingredient ingredient = new Ingredient();
        when(repository.save(any())).thenReturn(ingredient);
        assertNotNull(dataAccess.save(ingredient));
    }

    @Test
    void findById() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Ingredient()));
        assertTrue(dataAccess.findById(1L).isPresent());
    }

    @Test
    void getById() {
        Ingredient ingredient = new Ingredient();
        when(repository.findById(1L)).thenReturn(Optional.of(ingredient));
        assertNotNull(dataAccess.getById(1L));
    }

    @Test
    void findAll() {
        when(repository.findAll()).thenReturn(Collections.emptyList());
        assertNotNull(dataAccess.findAll());
    }

    @Test
    void findAllById() {
        when(repository.findAllById(any(List.class))).thenReturn(Collections.emptyList());
        assertNotNull(dataAccess.findAllById(List.of(1L)));
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
