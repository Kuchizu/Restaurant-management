package ru.ifmo.se.restaurant.kitchen.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.kitchen.repository.KitchenQueueRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.kitchen.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class KitchenQueueDataAccessTest {

    @Mock
    private KitchenQueueRepository kitchenQueueRepository;

    @InjectMocks
    private KitchenQueueDataAccess kitchenQueueDataAccess;

    @Test
    void findById_WhenExists_ReturnsOptionalWithKitchenQueue() {
        // Given
        Long queueId = 1L;
        KitchenQueue queue = createMockKitchenQueue(queueId);
        when(kitchenQueueRepository.findById(queueId)).thenReturn(Optional.of(queue));

        // When
        Optional<KitchenQueue> result = kitchenQueueDataAccess.findById(queueId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(queueId);
        assertThat(result.get().getDishName()).isEqualTo("Test Dish");
        verify(kitchenQueueRepository).findById(queueId);
    }

    @Test
    void findById_WhenNotExists_ReturnsEmptyOptional() {
        // Given
        Long queueId = 999L;
        when(kitchenQueueRepository.findById(queueId)).thenReturn(Optional.empty());

        // When
        Optional<KitchenQueue> result = kitchenQueueDataAccess.findById(queueId);

        // Then
        assertThat(result).isEmpty();
        verify(kitchenQueueRepository).findById(queueId);
    }

    @Test
    void getById_WhenExists_ReturnsKitchenQueue() {
        // Given
        Long queueId = 1L;
        KitchenQueue queue = createMockKitchenQueue(queueId);
        when(kitchenQueueRepository.findById(queueId)).thenReturn(Optional.of(queue));

        // When
        KitchenQueue result = kitchenQueueDataAccess.getById(queueId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(queueId);
        assertThat(result.getStatus()).isEqualTo(DishStatus.PENDING);
        verify(kitchenQueueRepository).findById(queueId);
    }

    @Test
    void getById_WhenNotExists_ThrowsException() {
        // Given
        Long queueId = 999L;
        when(kitchenQueueRepository.findById(queueId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> kitchenQueueDataAccess.getById(queueId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Kitchen queue item not found with id: " + queueId);

        verify(kitchenQueueRepository).findById(queueId);
    }

    @Test
    void save_SavesKitchenQueueAndReturns() {
        // Given
        KitchenQueue queue = createMockKitchenQueue(null);
        KitchenQueue savedQueue = createMockKitchenQueue(1L);
        when(kitchenQueueRepository.save(queue)).thenReturn(savedQueue);

        // When
        KitchenQueue result = kitchenQueueDataAccess.save(queue);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDishName()).isEqualTo("Test Dish");
        verify(kitchenQueueRepository).save(queue);
    }

    @Test
    void findAll_ReturnsAllKitchenQueues() {
        // Given
        List<KitchenQueue> queues = Arrays.asList(
                createMockKitchenQueue(1L),
                createMockKitchenQueue(2L)
        );
        when(kitchenQueueRepository.findAll()).thenReturn(queues);

        // When
        List<KitchenQueue> result = kitchenQueueDataAccess.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(kitchenQueueRepository).findAll();
    }

    @Test
    void findByStatusInOrderByCreatedAtAsc_ReturnsMatchingQueues() {
        // Given
        List<DishStatus> statuses = Arrays.asList(DishStatus.PENDING, DishStatus.IN_PROGRESS);
        List<KitchenQueue> queues = Arrays.asList(
                createKitchenQueueWithStatus(1L, DishStatus.PENDING),
                createKitchenQueueWithStatus(2L, DishStatus.IN_PROGRESS)
        );
        when(kitchenQueueRepository.findByStatusInOrderByCreatedAtAsc(statuses)).thenReturn(queues);

        // When
        List<KitchenQueue> result = kitchenQueueDataAccess.findByStatusInOrderByCreatedAtAsc(statuses);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo(DishStatus.PENDING);
        assertThat(result.get(1).getStatus()).isEqualTo(DishStatus.IN_PROGRESS);
        verify(kitchenQueueRepository).findByStatusInOrderByCreatedAtAsc(statuses);
    }

    @Test
    void findByOrderId_ReturnsMatchingQueues() {
        // Given
        Long orderId = 100L;
        List<KitchenQueue> queues = Arrays.asList(
                createKitchenQueueForOrder(1L, orderId, "Dish 1"),
                createKitchenQueueForOrder(2L, orderId, "Dish 2")
        );
        when(kitchenQueueRepository.findByOrderId(orderId)).thenReturn(queues);

        // When
        List<KitchenQueue> result = kitchenQueueDataAccess.findByOrderId(orderId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(q -> q.getOrderId().equals(orderId));
        verify(kitchenQueueRepository).findByOrderId(orderId);
    }

    @Test
    void existsById_WhenExists_ReturnsTrue() {
        // Given
        Long queueId = 1L;
        when(kitchenQueueRepository.existsById(queueId)).thenReturn(true);

        // When
        boolean result = kitchenQueueDataAccess.existsById(queueId);

        // Then
        assertThat(result).isTrue();
        verify(kitchenQueueRepository).existsById(queueId);
    }

    @Test
    void existsById_WhenNotExists_ReturnsFalse() {
        // Given
        Long queueId = 999L;
        when(kitchenQueueRepository.existsById(queueId)).thenReturn(false);

        // When
        boolean result = kitchenQueueDataAccess.existsById(queueId);

        // Then
        assertThat(result).isFalse();
        verify(kitchenQueueRepository).existsById(queueId);
    }

    @Test
    void deleteById_DeletesKitchenQueue() {
        // Given
        Long queueId = 1L;
        doNothing().when(kitchenQueueRepository).deleteById(queueId);

        // When
        kitchenQueueDataAccess.deleteById(queueId);

        // Then
        verify(kitchenQueueRepository).deleteById(queueId);
    }
}
