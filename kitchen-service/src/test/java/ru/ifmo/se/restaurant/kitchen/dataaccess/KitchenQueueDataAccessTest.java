package ru.ifmo.se.restaurant.kitchen.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.repository.KitchenQueueRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KitchenQueueDataAccessTest {

    @Mock
    private KitchenQueueRepository repository;

    @InjectMocks
    private KitchenQueueDataAccess dataAccess;

    @Test
    void save() {
        KitchenQueue queue = new KitchenQueue();
        when(repository.save(any())).thenReturn(queue);
        assertNotNull(dataAccess.save(queue));
    }

    @Test
    void findById() {
        when(repository.findById(1L)).thenReturn(Optional.of(new KitchenQueue()));
        assertTrue(dataAccess.findById(1L).isPresent());
    }

    @Test
    void getById() {
        KitchenQueue queue = new KitchenQueue();
        when(repository.findById(1L)).thenReturn(Optional.of(queue));
        assertNotNull(dataAccess.getById(1L));
    }

    @Test
    void findAll() {
        when(repository.findAll()).thenReturn(Collections.emptyList());
        assertNotNull(dataAccess.findAll());
    }

    @Test
    void deleteById() {
        doNothing().when(repository).deleteById(1L);
        dataAccess.deleteById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void findByStatusInOrderByCreatedAtAsc() {
        when(repository.findByStatusInOrderByCreatedAtAsc(any())).thenReturn(Collections.emptyList());
        assertNotNull(dataAccess.findByStatusInOrderByCreatedAtAsc(List.of(DishStatus.PENDING)));
    }

    @Test
    void findByOrderId() {
        when(repository.findByOrderId(1L)).thenReturn(Collections.emptyList());
        assertNotNull(dataAccess.findByOrderId(1L));
    }

    @Test
    void existsById() {
        when(repository.existsById(1L)).thenReturn(true);
        assertTrue(dataAccess.existsById(1L));
    }

    @Test
    void findAllPaginated() {
        Page<KitchenQueue> page = new PageImpl<>(Collections.emptyList());
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);
        assertNotNull(dataAccess.findAll(PageRequest.of(0, 20)));
    }

    @Test
    void findAllSlice() {
        Page<KitchenQueue> page = new PageImpl<>(Collections.emptyList());
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);
        Slice<KitchenQueue> slice = dataAccess.findAllSlice(PageRequest.of(0, 20));
        assertNotNull(slice);
    }

    @Test
    void findByStatus() {
        Page<KitchenQueue> page = new PageImpl<>(Collections.emptyList());
        when(repository.findByStatus(any(), any())).thenReturn(page);
        assertNotNull(dataAccess.findByStatus(DishStatus.PENDING, PageRequest.of(0, 20)));
    }
}
