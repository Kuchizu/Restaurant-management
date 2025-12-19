package ru.ifmo.se.restaurant.menu.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.ifmo.se.restaurant.menu.entity.Category;
import ru.ifmo.se.restaurant.menu.repository.CategoryRepository;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryDataAccessTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryDataAccess dataAccess;

    @Test
    void save() {
        Category category = new Category();
        when(repository.save(any())).thenReturn(category);
        assertNotNull(dataAccess.save(category));
    }

    @Test
    void findById() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Category()));
        assertTrue(dataAccess.findById(1L).isPresent());
    }

    @Test
    void getById() {
        Category category = new Category();
        when(repository.findById(1L)).thenReturn(Optional.of(category));
        assertNotNull(dataAccess.getById(1L));
    }

    @Test
    void findAll() {
        when(repository.findAll()).thenReturn(Collections.emptyList());
        assertNotNull(dataAccess.findAll());
    }

    @Test
    void findAllPaginated() {
        Page<Category> page = new PageImpl<>(Collections.emptyList());
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);
        assertNotNull(dataAccess.findAll(PageRequest.of(0, 20)));
    }

    @Test
    void findAllSlice() {
        Page<Category> page = new PageImpl<>(Collections.emptyList());
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);
        assertNotNull(dataAccess.findAllSlice(PageRequest.of(0, 20)));
    }

    @Test
    void findByName() {
        when(repository.findByName("Test")).thenReturn(Optional.of(new Category()));
        assertTrue(dataAccess.findByName("Test").isPresent());
    }

    @Test
    void findByIsActive() {
        when(repository.findByIsActive(true)).thenReturn(Collections.emptyList());
        assertNotNull(dataAccess.findByIsActive(true));
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
