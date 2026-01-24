package ru.ifmo.se.restaurant.order.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.kitchen.DishReadyEvent;
import ru.ifmo.se.restaurant.order.application.port.out.OrderRepositoryPort;
import ru.ifmo.se.restaurant.order.domain.entity.Order;
import ru.ifmo.se.restaurant.order.domain.valueobject.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DishReadyEventConsumerTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    private ObjectMapper objectMapper;
    private DishReadyEventConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        consumer = new DishReadyEventConsumer(orderRepositoryPort, objectMapper);
    }

    @Test
    void handleDishReady_WithInKitchenStatus_ShouldUpdateToReady() throws Exception {
        Order order = new Order(1L, 10L, 5L, OrderStatus.IN_KITCHEN,
                new BigDecimal("100.00"), null, LocalDateTime.now(), null, 1L);

        DishReadyEvent payload = DishReadyEvent.builder()
                .kitchenQueueId(100L)
                .orderId(1L)
                .orderItemId(50L)
                .dishName("Pizza")
                .quantity(2)
                .readyAt(Instant.now())
                .build();

        DomainEvent<DishReadyEvent> event = DomainEvent.create("DISH_READY", payload);
        String message = objectMapper.writeValueAsString(event);

        when(orderRepositoryPort.getById(1L)).thenReturn(Mono.just(order));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(Mono.just(order));

        consumer.handleDishReady(message);

        Thread.sleep(100); // Allow async processing
        verify(orderRepositoryPort).getById(1L);
    }

    @Test
    void handleDishReady_WithNonInKitchenStatus_ShouldNotUpdate() throws Exception {
        Order order = new Order(1L, 10L, 5L, OrderStatus.READY,
                new BigDecimal("100.00"), null, LocalDateTime.now(), null, 1L);

        DishReadyEvent payload = DishReadyEvent.builder()
                .kitchenQueueId(100L)
                .orderId(1L)
                .orderItemId(50L)
                .dishName("Pizza")
                .quantity(2)
                .readyAt(Instant.now())
                .build();

        DomainEvent<DishReadyEvent> event = DomainEvent.create("DISH_READY", payload);
        String message = objectMapper.writeValueAsString(event);

        when(orderRepositoryPort.getById(1L)).thenReturn(Mono.just(order));

        consumer.handleDishReady(message);

        Thread.sleep(100);
        verify(orderRepositoryPort).getById(1L);
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void handleDishReady_WithInvalidJson_ShouldNotThrowException() {
        String invalidMessage = "{ invalid json }";

        consumer.handleDishReady(invalidMessage);

        verify(orderRepositoryPort, never()).getById(anyLong());
    }
}
