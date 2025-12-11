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
import ru.ifmo.se.restaurant.order.entity.Order;
import ru.ifmo.se.restaurant.order.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.repository.OrderRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDataAccessTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderDataAccess orderDataAccess;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = TestDataFactory.createDefaultOrder();
    }

    @Test
    void findById_ShouldReturnOrder_WhenOrderExists() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Mono.just(testOrder));

        // Act & Assert
        StepVerifier.create(orderDataAccess.findById(1L))
            .expectNext(testOrder)
            .verifyComplete();

        verify(orderRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenOrderNotExists() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderDataAccess.findById(1L))
            .verifyComplete();

        verify(orderRepository).findById(1L);
    }

    @Test
    void getById_ShouldReturnOrder_WhenOrderExists() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Mono.just(testOrder));

        // Act & Assert
        StepVerifier.create(orderDataAccess.getById(1L))
            .expectNext(testOrder)
            .verifyComplete();

        verify(orderRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowException_WhenOrderNotExists() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderDataAccess.getById(1L))
            .expectError(ResourceNotFoundException.class)
            .verify();

        verify(orderRepository).findById(1L);
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        // Arrange
        Order order2 = TestDataFactory.createOrder(2L, 2L, 1L,
            ru.ifmo.se.restaurant.order.entity.OrderStatus.CREATED, java.math.BigDecimal.ZERO);
        when(orderRepository.findAll()).thenReturn(Flux.just(testOrder, order2));

        // Act & Assert
        StepVerifier.create(orderDataAccess.findAll())
            .expectNext(testOrder)
            .expectNext(order2)
            .verifyComplete();

        verify(orderRepository).findAll();
    }

    @Test
    void save_ShouldPersistOrder() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(testOrder));

        // Act & Assert
        StepVerifier.create(orderDataAccess.save(testOrder))
            .expectNext(testOrder)
            .verifyComplete();

        verify(orderRepository).save(testOrder);
    }

    @Test
    void deleteById_ShouldDeleteOrder() {
        // Arrange
        when(orderRepository.deleteById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderDataAccess.deleteById(1L))
            .verifyComplete();

        verify(orderRepository).deleteById(1L);
    }
}
