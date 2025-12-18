package ru.ifmo.se.restaurant.kitchen.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.ifmo.se.restaurant.kitchen.dataaccess.KitchenQueueDataAccess;
import ru.ifmo.se.restaurant.kitchen.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.exception.ResourceNotFoundException;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.kitchen.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class KitchenServiceTest {

    @Mock
    private KitchenQueueDataAccess kitchenQueueDataAccess;

    @InjectMocks
    private KitchenService kitchenService;

    @Test
    void addToQueue_CreatesNewQueueItem() {
        // Given
        KitchenQueueDto inputDto = createKitchenQueueDtoForCreation();
        KitchenQueue savedQueue = createMockKitchenQueue(1L);
        savedQueue.setDishName("New Dish");
        savedQueue.setQuantity(1);
        savedQueue.setSpecialRequest("Extra spicy");

        when(kitchenQueueDataAccess.save(any(KitchenQueue.class))).thenReturn(savedQueue);

        // When
        Mono<KitchenQueueDto> result = kitchenService.addToQueue(inputDto);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        dto.getId() != null &&
                        dto.getOrderId().equals(100L) &&
                        dto.getDishName().equals("New Dish") &&
                        dto.getQuantity().equals(1) &&
                        dto.getStatus() == DishStatus.PENDING
                )
                .verifyComplete();

        verify(kitchenQueueDataAccess).save(any(KitchenQueue.class));
    }

    @Test
    void getActiveQueue_ReturnsActiveItems() {
        // Given
        List<DishStatus> activeStatuses = Arrays.asList(DishStatus.PENDING, DishStatus.IN_PROGRESS);
        List<KitchenQueue> queues = Arrays.asList(
                createKitchenQueueWithStatus(1L, DishStatus.PENDING),
                createKitchenQueueWithStatus(2L, DishStatus.IN_PROGRESS)
        );
        when(kitchenQueueDataAccess.findByStatusInOrderByCreatedAtAsc(activeStatuses)).thenReturn(queues);

        // When
        Flux<KitchenQueueDto> result = kitchenService.getActiveQueue();

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getStatus() == DishStatus.PENDING)
                .expectNextMatches(dto -> dto.getStatus() == DishStatus.IN_PROGRESS)
                .verifyComplete();

        verify(kitchenQueueDataAccess).findByStatusInOrderByCreatedAtAsc(activeStatuses);
    }

    @Test
    void getAllQueue_ReturnsAllItems() {
        // Given
        List<KitchenQueue> queues = Arrays.asList(
                createMockKitchenQueue(1L),
                createMockKitchenQueue(2L),
                createMockKitchenQueue(3L)
        );
        when(kitchenQueueDataAccess.findAll()).thenReturn(queues);

        // When
        Flux<KitchenQueueDto> result = kitchenService.getAllQueue();

        // Then
        StepVerifier.create(result)
                .expectNextCount(3)
                .verifyComplete();

        verify(kitchenQueueDataAccess).findAll();
    }

    @Test
    void getQueueItemById_WhenExists_ReturnsItem() {
        // Given
        Long queueId = 1L;
        KitchenQueue queue = createMockKitchenQueue(queueId);
        when(kitchenQueueDataAccess.getById(queueId)).thenReturn(queue);

        // When
        Mono<KitchenQueueDto> result = kitchenService.getQueueItemById(queueId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        dto.getId().equals(queueId) &&
                        dto.getDishName().equals("Test Dish") &&
                        dto.getStatus() == DishStatus.PENDING
                )
                .verifyComplete();

        verify(kitchenQueueDataAccess).getById(queueId);
    }

    @Test
    void getQueueItemById_WhenNotExists_ThrowsException() {
        // Given
        Long queueId = 999L;
        when(kitchenQueueDataAccess.getById(queueId))
                .thenThrow(new ResourceNotFoundException("Kitchen queue item not found with id: " + queueId));

        // When
        Mono<KitchenQueueDto> result = kitchenService.getQueueItemById(queueId);

        // Then
        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(kitchenQueueDataAccess).getById(queueId);
    }

    @Test
    void updateStatus_ToInProgress_UpdatesStatusAndStartTime() {
        // Given
        Long queueId = 1L;
        KitchenQueue queue = createKitchenQueueWithStatus(queueId, DishStatus.PENDING);
        queue.setStartedAt(null); // Reset to null to test setting

        when(kitchenQueueDataAccess.getById(queueId)).thenReturn(queue);
        when(kitchenQueueDataAccess.save(any(KitchenQueue.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Mono<KitchenQueueDto> result = kitchenService.updateStatus(queueId, DishStatus.IN_PROGRESS);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        dto.getStatus() == DishStatus.IN_PROGRESS &&
                        dto.getStartedAt() != null
                )
                .verifyComplete();

        verify(kitchenQueueDataAccess).getById(queueId);
        verify(kitchenQueueDataAccess).save(argThat(q ->
                q.getStatus() == DishStatus.IN_PROGRESS && q.getStartedAt() != null
        ));
    }

    @Test
    void updateStatus_ToReady_UpdatesStatusAndCompletedTime() {
        // Given
        Long queueId = 1L;
        KitchenQueue queue = createKitchenQueueWithStatus(queueId, DishStatus.IN_PROGRESS);
        queue.setCompletedAt(null); // Reset to null to test setting

        when(kitchenQueueDataAccess.getById(queueId)).thenReturn(queue);
        when(kitchenQueueDataAccess.save(any(KitchenQueue.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Mono<KitchenQueueDto> result = kitchenService.updateStatus(queueId, DishStatus.READY);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        dto.getStatus() == DishStatus.READY &&
                        dto.getCompletedAt() != null
                )
                .verifyComplete();

        verify(kitchenQueueDataAccess).getById(queueId);
        verify(kitchenQueueDataAccess).save(argThat(q ->
                q.getStatus() == DishStatus.READY && q.getCompletedAt() != null
        ));
    }

    @Test
    void updateStatus_ToServed_UpdatesStatus() {
        // Given
        Long queueId = 1L;
        KitchenQueue queue = createKitchenQueueWithStatus(queueId, DishStatus.READY);

        when(kitchenQueueDataAccess.getById(queueId)).thenReturn(queue);
        when(kitchenQueueDataAccess.save(any(KitchenQueue.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Mono<KitchenQueueDto> result = kitchenService.updateStatus(queueId, DishStatus.SERVED);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getStatus() == DishStatus.SERVED)
                .verifyComplete();

        verify(kitchenQueueDataAccess).getById(queueId);
        verify(kitchenQueueDataAccess).save(any(KitchenQueue.class));
    }

    @Test
    void updateStatus_WhenItemNotFound_ThrowsException() {
        // Given
        Long queueId = 999L;
        when(kitchenQueueDataAccess.getById(queueId))
                .thenThrow(new ResourceNotFoundException("Kitchen queue item not found with id: " + queueId));

        // When
        Mono<KitchenQueueDto> result = kitchenService.updateStatus(queueId, DishStatus.IN_PROGRESS);

        // Then
        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(kitchenQueueDataAccess).getById(queueId);
        verify(kitchenQueueDataAccess, never()).save(any());
    }

    @Test
    void getQueueByOrderId_ReturnsMatchingItems() {
        // Given
        Long orderId = 100L;
        List<KitchenQueue> queues = Arrays.asList(
                createKitchenQueueForOrder(1L, orderId, "Dish 1"),
                createKitchenQueueForOrder(2L, orderId, "Dish 2"),
                createKitchenQueueForOrder(3L, orderId, "Dish 3")
        );
        when(kitchenQueueDataAccess.findByOrderId(orderId)).thenReturn(queues);

        // When
        Flux<KitchenQueueDto> result = kitchenService.getQueueByOrderId(orderId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getOrderId().equals(orderId) && dto.getDishName().equals("Dish 1"))
                .expectNextMatches(dto -> dto.getOrderId().equals(orderId) && dto.getDishName().equals("Dish 2"))
                .expectNextMatches(dto -> dto.getOrderId().equals(orderId) && dto.getDishName().equals("Dish 3"))
                .verifyComplete();

        verify(kitchenQueueDataAccess).findByOrderId(orderId);
    }

    @Test
    void getQueueByOrderId_WhenNoItems_ReturnsEmpty() {
        // Given
        Long orderId = 999L;
        when(kitchenQueueDataAccess.findByOrderId(orderId)).thenReturn(Arrays.asList());

        // When
        Flux<KitchenQueueDto> result = kitchenService.getQueueByOrderId(orderId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(kitchenQueueDataAccess).findByOrderId(orderId);
    }
}
