package ru.ifmo.se.restaurant.kitchen.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.kitchen.application.port.out.KitchenEventPublisher;
import ru.ifmo.se.restaurant.kitchen.application.port.out.KitchenQueueRepository;
import ru.ifmo.se.restaurant.kitchen.application.port.out.MenuServicePort;
import ru.ifmo.se.restaurant.kitchen.application.dto.DishInfoDto;
import ru.ifmo.se.restaurant.kitchen.application.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.application.usecase.KitchenService;
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KitchenServiceTest {

    @Mock
    private KitchenQueueRepository repository;

    @Mock
    private MenuServicePort menuServicePort;

    @Mock
    private KitchenEventPublisher eventPublisher;

    @InjectMocks
    private KitchenService service;

    @Test
    void addToQueue() {
        KitchenQueue queue = KitchenQueue.builder()
                .id(1L)
                .orderId(1L)
                .orderItemId(1L)
                .dishName("Test Dish")
                .quantity(1)
                .status(DishStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        when(repository.save(any())).thenReturn(queue);
        when(menuServicePort.getDishByName(any())).thenReturn(createDishInfoDto());

        KitchenQueueDto result = service.addToQueue(new KitchenQueueDto());
        assertNotNull(result);
    }

    @Test
    void getActiveQueue() {
        when(repository.findByStatusInOrderByCreatedAtAsc(any())).thenReturn(Collections.emptyList());
        assertNotNull(service.getActiveQueue());
    }

    @Test
    void getAllQueue() {
        when(repository.findAll()).thenReturn(Collections.emptyList());
        assertNotNull(service.getAllQueue());
    }

    @Test
    void getQueueItemById() {
        KitchenQueue queue = KitchenQueue.builder()
                .id(1L)
                .orderId(1L)
                .orderItemId(1L)
                .dishName("Test Dish")
                .quantity(1)
                .status(DishStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(queue));
        assertNotNull(service.getQueueItemById(1L));
    }

    @Test
    void updateStatus() {
        KitchenQueue queue = KitchenQueue.builder()
                .id(1L)
                .orderId(1L)
                .orderItemId(1L)
                .dishName("Test Dish")
                .quantity(1)
                .status(DishStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(queue));
        when(repository.save(any())).thenReturn(queue);
        assertNotNull(service.updateStatus(1L, DishStatus.IN_PROGRESS));
    }

    @Test
    void updateStatusToReady() {
        KitchenQueue queue = KitchenQueue.builder()
                .id(1L)
                .orderId(1L)
                .orderItemId(1L)
                .dishName("Test Dish")
                .quantity(1)
                .status(DishStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(queue));
        when(repository.save(any())).thenReturn(queue);
        assertNotNull(service.updateStatus(1L, DishStatus.READY));
    }

    @Test
    void addToQueueWithQuantity() {
        KitchenQueue queue = KitchenQueue.builder()
                .id(1L)
                .orderId(1L)
                .orderItemId(1L)
                .dishName("Test Dish")
                .quantity(5)
                .status(DishStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        when(repository.save(any())).thenReturn(queue);
        when(menuServicePort.getDishByName(any())).thenReturn(createDishInfoDto());
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setQuantity(5);
        assertNotNull(service.addToQueue(dto));
    }

    @Test
    void getQueueByOrderId() {
        when(repository.findByOrderId(1L)).thenReturn(Collections.emptyList());
        assertNotNull(service.getQueueByOrderId(1L));
    }

    @Test
    void getAllQueueItemsPaginated() {
        org.springframework.data.domain.Page<KitchenQueue> page = org.springframework.data.domain.Page.empty();
        when(repository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        assertNotNull(service.getAllQueueItemsPaginated(0, 20));
    }

    @Test
    void getAllQueueItemsSlice() {
        org.springframework.data.domain.Slice<KitchenQueue> slice = new org.springframework.data.domain.SliceImpl<>(Collections.emptyList());
        when(repository.findAllSlice(any(org.springframework.data.domain.Pageable.class))).thenReturn(slice);
        assertNotNull(service.getAllQueueItemsSlice(0, 20));
    }

    private DishInfoDto createDishInfoDto() {
        return new DishInfoDto(1L, "Test Dish", "Description", BigDecimal.TEN, 1L, "Category", true, Collections.emptyList());
    }
}
