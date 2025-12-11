package ru.ifmo.se.restaurant.order.dataaccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.ifmo.se.restaurant.order.TestDataFactory;
import ru.ifmo.se.restaurant.order.entity.OrderItem;
import ru.ifmo.se.restaurant.order.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.repository.OrderItemRepository;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemDataAccessTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemDataAccess orderItemDataAccess;

    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testOrderItem = TestDataFactory.createDefaultOrderItem();
    }

    @Test
    void findById_ShouldReturnOrderItem_WhenExists() {
        // Arrange
        when(orderItemRepository.findById(anyLong())).thenReturn(Mono.just(testOrderItem));

        // Act & Assert
        StepVerifier.create(orderItemDataAccess.findById(1L))
            .expectNext(testOrderItem)
            .verifyComplete();

        verify(orderItemRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // Arrange
        when(orderItemRepository.findById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderItemDataAccess.findById(1L))
            .verifyComplete();

        verify(orderItemRepository).findById(1L);
    }

    @Test
    void getById_ShouldReturnOrderItem_WhenExists() {
        // Arrange
        when(orderItemRepository.findById(anyLong())).thenReturn(Mono.just(testOrderItem));

        // Act & Assert
        StepVerifier.create(orderItemDataAccess.getById(1L))
            .expectNext(testOrderItem)
            .verifyComplete();

        verify(orderItemRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(orderItemRepository.findById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderItemDataAccess.getById(1L))
            .expectError(ResourceNotFoundException.class)
            .verify();

        verify(orderItemRepository).findById(1L);
    }

    @Test
    void findByOrderId_ShouldReturnAllItemsForOrder() {
        // Arrange
        OrderItem item2 = TestDataFactory.createOrderItem(2L, 1L, 2L,
            "Pizza Margherita", 1, new BigDecimal("15.00"));
        when(orderItemRepository.findByOrderId(anyLong()))
            .thenReturn(Flux.just(testOrderItem, item2));

        // Act & Assert
        StepVerifier.create(orderItemDataAccess.findByOrderId(1L))
            .expectNext(testOrderItem)
            .expectNext(item2)
            .verifyComplete();

        verify(orderItemRepository).findByOrderId(1L);
    }

    @Test
    void findByOrderId_ShouldReturnEmpty_WhenNoItems() {
        // Arrange
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(orderItemDataAccess.findByOrderId(1L))
            .verifyComplete();

        verify(orderItemRepository).findByOrderId(1L);
    }

    @Test
    void findAll_ShouldReturnAllOrderItems() {
        // Arrange
        OrderItem item2 = TestDataFactory.createOrderItem(2L, 1L, 2L,
            "Pizza Margherita", 1, new BigDecimal("15.00"));
        when(orderItemRepository.findAll()).thenReturn(Flux.just(testOrderItem, item2));

        // Act & Assert
        StepVerifier.create(orderItemDataAccess.findAll())
            .expectNext(testOrderItem)
            .expectNext(item2)
            .verifyComplete();

        verify(orderItemRepository).findAll();
    }

    @Test
    void save_ShouldPersistOrderItem() {
        // Arrange
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(testOrderItem));

        // Act & Assert
        StepVerifier.create(orderItemDataAccess.save(testOrderItem))
            .expectNext(testOrderItem)
            .verifyComplete();

        verify(orderItemRepository).save(testOrderItem);
    }

    @Test
    void deleteById_ShouldDeleteOrderItem() {
        // Arrange
        when(orderItemRepository.deleteById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderItemDataAccess.deleteById(1L))
            .verifyComplete();

        verify(orderItemRepository).deleteById(1L);
    }

    @Test
    void deleteByOrderId_ShouldDeleteAllItemsForOrder() {
        // Arrange
        when(orderItemRepository.deleteByOrderId(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderItemDataAccess.deleteByOrderId(1L))
            .verifyComplete();

        verify(orderItemRepository).deleteByOrderId(1L);
    }
}
