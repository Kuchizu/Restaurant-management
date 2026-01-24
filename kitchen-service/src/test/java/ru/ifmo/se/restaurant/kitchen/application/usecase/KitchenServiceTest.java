package ru.ifmo.se.restaurant.kitchen.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.ifmo.se.restaurant.kitchen.application.dto.DishInfoDto;
import ru.ifmo.se.restaurant.kitchen.application.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.application.port.out.KitchenEventPublisher;
import ru.ifmo.se.restaurant.kitchen.application.port.out.KitchenQueueRepository;
import ru.ifmo.se.restaurant.kitchen.application.port.out.MenuServicePort;
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.domain.exception.KitchenQueueNotFoundException;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KitchenServiceTest {

    @Mock
    private KitchenQueueRepository kitchenQueueRepository;

    @Mock
    private MenuServicePort menuServicePort;

    @Mock
    private KitchenEventPublisher kitchenEventPublisher;

    @InjectMocks
    private KitchenService kitchenService;

    private KitchenQueue testQueue;
    private DishInfoDto testDishInfo;

    @BeforeEach
    void setUp() {
        testQueue = KitchenQueue.builder()
                .id(1L)
                .orderId(100L)
                .orderItemId(10L)
                .dishName("Margherita Pizza")
                .quantity(2)
                .status(DishStatus.PENDING)
                .specialRequest("Extra cheese")
                .createdAt(LocalDateTime.now())
                .build();

        testDishInfo = new DishInfoDto(1L, "Margherita Pizza", "Delicious pizza", new BigDecimal("15.00"), 1L, "Italian", true, Collections.emptyList());
    }

    @Test
    void addToQueue_ShouldCreateQueueItem() {
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setOrderId(100L);
        dto.setOrderItemId(10L);
        dto.setDishName("Margherita Pizza");
        dto.setQuantity(2);
        dto.setSpecialRequest("Extra cheese");

        when(menuServicePort.getDishByName("Margherita Pizza")).thenReturn(testDishInfo);
        when(kitchenQueueRepository.save(any(KitchenQueue.class))).thenReturn(testQueue);

        KitchenQueueDto result = kitchenService.addToQueue(dto);

        assertNotNull(result);
        assertEquals("Margherita Pizza", result.getDishName());
        verify(menuServicePort).getDishByName("Margherita Pizza");
        verify(kitchenQueueRepository).save(any(KitchenQueue.class));
    }

    @Test
    void getActiveQueue_ShouldReturnActiveItems() {
        List<KitchenQueue> activeItems = Arrays.asList(testQueue);
        when(kitchenQueueRepository.findByStatusInOrderByCreatedAtAsc(any())).thenReturn(activeItems);

        List<KitchenQueueDto> result = kitchenService.getActiveQueue();

        assertEquals(1, result.size());
        assertEquals("Margherita Pizza", result.get(0).getDishName());
    }

    @Test
    void getAllQueue_ShouldReturnAllItems() {
        when(kitchenQueueRepository.findAll()).thenReturn(Arrays.asList(testQueue));

        List<KitchenQueueDto> result = kitchenService.getAllQueue();

        assertEquals(1, result.size());
    }

    @Test
    void getQueueItemById_ShouldReturnItem() {
        when(kitchenQueueRepository.findById(1L)).thenReturn(Optional.of(testQueue));

        KitchenQueueDto result = kitchenService.getQueueItemById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Margherita Pizza", result.getDishName());
    }

    @Test
    void getQueueItemById_ShouldThrow_WhenNotFound() {
        when(kitchenQueueRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(KitchenQueueNotFoundException.class, () -> kitchenService.getQueueItemById(999L));
    }

    @Test
    void updateStatus_ShouldUpdateStatus() {
        KitchenQueue updatedQueue = testQueue.withStatus(DishStatus.IN_PROGRESS);
        when(kitchenQueueRepository.findById(1L)).thenReturn(Optional.of(testQueue));
        when(kitchenQueueRepository.save(any(KitchenQueue.class))).thenReturn(updatedQueue);

        KitchenQueueDto result = kitchenService.updateStatus(1L, DishStatus.IN_PROGRESS);

        assertNotNull(result);
        verify(kitchenQueueRepository).save(any(KitchenQueue.class));
    }

    @Test
    void updateStatus_ShouldPublishEvent_WhenReady() {
        KitchenQueue readyQueue = testQueue.withStatus(DishStatus.READY);
        when(kitchenQueueRepository.findById(1L)).thenReturn(Optional.of(testQueue));
        when(kitchenQueueRepository.save(any(KitchenQueue.class))).thenReturn(readyQueue);
        doNothing().when(kitchenEventPublisher).publishDishReady(any());

        kitchenService.updateStatus(1L, DishStatus.READY);

        verify(kitchenEventPublisher).publishDishReady(any(KitchenQueue.class));
    }

    @Test
    void updateStatus_ShouldThrow_WhenNotFound() {
        when(kitchenQueueRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(KitchenQueueNotFoundException.class,
                () -> kitchenService.updateStatus(999L, DishStatus.IN_PROGRESS));
    }

    @Test
    void getQueueByOrderId_ShouldReturnItems() {
        when(kitchenQueueRepository.findByOrderId(100L)).thenReturn(Arrays.asList(testQueue));

        List<KitchenQueueDto> result = kitchenService.getQueueByOrderId(100L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getOrderId());
    }

    @Test
    void getAllQueueItemsPaginated_ShouldReturnPage() {
        Page<KitchenQueue> page = new PageImpl<>(Arrays.asList(testQueue));
        when(kitchenQueueRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<KitchenQueueDto> result = kitchenService.getAllQueueItemsPaginated(0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllQueueItemsSlice_ShouldReturnSlice() {
        Slice<KitchenQueue> slice = new SliceImpl<>(Arrays.asList(testQueue));
        when(kitchenQueueRepository.findAllSlice(any(Pageable.class))).thenReturn(slice);

        Slice<KitchenQueueDto> result = kitchenService.getAllQueueItemsSlice(0, 10);

        assertTrue(result.hasContent());
    }
}
