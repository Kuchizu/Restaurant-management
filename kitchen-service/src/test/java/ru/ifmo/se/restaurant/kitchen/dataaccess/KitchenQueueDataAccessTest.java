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
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;
import ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.persistence.KitchenQueueRepositoryAdapter;
import ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.persistence.entity.KitchenQueueJpaEntity;
import ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.persistence.repository.KitchenQueueJpaRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KitchenQueueDataAccessTest {

    @Mock
    private KitchenQueueJpaRepository jpaRepository;

    @InjectMocks
    private KitchenQueueRepositoryAdapter adapter;

    @Test
    void save() {
        KitchenQueue queue = KitchenQueue.builder()
                .id(1L)
                .orderId(1L)
                .orderItemId(1L)
                .dishName("Test")
                .quantity(1)
                .status(DishStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        KitchenQueueJpaEntity entity = KitchenQueueJpaEntity.fromDomain(queue);
        when(jpaRepository.save(any())).thenReturn(entity);
        assertNotNull(adapter.save(queue));
    }

    @Test
    void findById() {
        KitchenQueueJpaEntity entity = KitchenQueueJpaEntity.builder()
                .id(1L)
                .orderId(1L)
                .orderItemId(1L)
                .dishName("Test")
                .quantity(1)
                .status(DishStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        assertTrue(adapter.findById(1L).isPresent());
    }

    @Test
    void findAll() {
        when(jpaRepository.findAll()).thenReturn(Collections.emptyList());
        assertNotNull(adapter.findAll());
    }

    @Test
    void deleteById() {
        doNothing().when(jpaRepository).deleteById(1L);
        adapter.deleteById(1L);
        verify(jpaRepository).deleteById(1L);
    }

    @Test
    void findByStatusInOrderByCreatedAtAsc() {
        when(jpaRepository.findByStatusInOrderByCreatedAtAsc(any())).thenReturn(Collections.emptyList());
        assertNotNull(adapter.findByStatusInOrderByCreatedAtAsc(List.of(DishStatus.PENDING)));
    }

    @Test
    void findByOrderId() {
        when(jpaRepository.findByOrderId(1L)).thenReturn(Collections.emptyList());
        assertNotNull(adapter.findByOrderId(1L));
    }

    @Test
    void existsById() {
        when(jpaRepository.existsById(1L)).thenReturn(true);
        assertTrue(adapter.existsById(1L));
    }

    @Test
    void findAllPaginated() {
        Page<KitchenQueueJpaEntity> page = new PageImpl<>(Collections.emptyList());
        when(jpaRepository.findAll(any(PageRequest.class))).thenReturn(page);
        assertNotNull(adapter.findAll(PageRequest.of(0, 20)));
    }

    @Test
    void findAllSlice() {
        Page<KitchenQueueJpaEntity> page = new PageImpl<>(Collections.emptyList());
        when(jpaRepository.findAll(any(PageRequest.class))).thenReturn(page);
        Slice<KitchenQueue> slice = adapter.findAllSlice(PageRequest.of(0, 20));
        assertNotNull(slice);
    }

    @Test
    void findByStatus() {
        Page<KitchenQueueJpaEntity> page = new PageImpl<>(Collections.emptyList());
        when(jpaRepository.findByStatus(any(), any())).thenReturn(page);
        assertNotNull(adapter.findByStatus(DishStatus.PENDING, PageRequest.of(0, 20)));
    }
}
