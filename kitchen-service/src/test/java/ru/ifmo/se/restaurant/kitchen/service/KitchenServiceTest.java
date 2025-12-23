package ru.ifmo.se.restaurant.kitchen.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.kitchen.client.MenuServiceClient;
import ru.ifmo.se.restaurant.kitchen.dataaccess.KitchenQueueDataAccess;
import ru.ifmo.se.restaurant.kitchen.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.entity.KitchenQueue;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KitchenServiceTest {

    @Mock
    private KitchenQueueDataAccess dataAccess;

    @Mock
    private MenuServiceClient menuServiceClient;

    @InjectMocks
    private KitchenService service;

    @Test
    void addToQueue() {
        KitchenQueue queue = new KitchenQueue();
        queue.setId(1L);
        when(dataAccess.save(any())).thenReturn(queue);

        KitchenQueueDto result = service.addToQueue(new KitchenQueueDto());
        assertNotNull(result);
    }

    @Test
    void getActiveQueue() {
        when(dataAccess.findByStatusInOrderByCreatedAtAsc(any())).thenReturn(Collections.emptyList());
        assertNotNull(service.getActiveQueue());
    }

    @Test
    void getAllQueue() {
        when(dataAccess.findAll()).thenReturn(Collections.emptyList());
        assertNotNull(service.getAllQueue());
    }

    @Test
    void getQueueItemById() {
        KitchenQueue queue = new KitchenQueue();
        queue.setId(1L);
        queue.setStatus(DishStatus.PENDING);
        queue.setCreatedAt(LocalDateTime.now());
        when(dataAccess.getById(1L)).thenReturn(queue);
        assertNotNull(service.getQueueItemById(1L));
    }

    @Test
    void updateStatus() {
        KitchenQueue queue = new KitchenQueue();
        queue.setId(1L);
        queue.setStatus(DishStatus.PENDING);
        queue.setCreatedAt(LocalDateTime.now());
        when(dataAccess.getById(1L)).thenReturn(queue);
        when(dataAccess.save(any())).thenReturn(queue);
        assertNotNull(service.updateStatus(1L, DishStatus.IN_PROGRESS));
    }

    @Test
    void updateStatusToReady() {
        KitchenQueue queue = new KitchenQueue();
        queue.setId(1L);
        queue.setStatus(DishStatus.IN_PROGRESS);
        queue.setCreatedAt(LocalDateTime.now());
        queue.setStartedAt(LocalDateTime.now());
        when(dataAccess.getById(1L)).thenReturn(queue);
        when(dataAccess.save(any())).thenReturn(queue);
        assertNotNull(service.updateStatus(1L, DishStatus.READY));
    }

    @Test
    void addToQueueWithQuantity() {
        KitchenQueue queue = new KitchenQueue();
        queue.setId(1L);
        when(dataAccess.save(any())).thenReturn(queue);
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setQuantity(5);
        assertNotNull(service.addToQueue(dto));
    }

    @Test
    void getQueueByOrderId() {
        when(dataAccess.findByOrderId(1L)).thenReturn(Collections.emptyList());
        assertNotNull(service.getQueueByOrderId(1L));
    }

    @Test
    void getAllQueueItemsPaginated() {
        org.springframework.data.domain.Page<KitchenQueue> page = org.springframework.data.domain.Page.empty();
        when(dataAccess.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        assertNotNull(service.getAllQueueItemsPaginated(0, 20));
    }

    @Test
    void getAllQueueItemsSlice() {
        org.springframework.data.domain.Slice<KitchenQueue> slice = new org.springframework.data.domain.SliceImpl<>(Collections.emptyList());
        when(dataAccess.findAllSlice(any(org.springframework.data.domain.Pageable.class))).thenReturn(slice);
        assertNotNull(service.getAllQueueItemsSlice(0, 20));
    }
}
