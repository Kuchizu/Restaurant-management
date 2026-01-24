package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.KafkaTopics;
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaKitchenEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaKitchenEventPublisher publisher;

    private KitchenQueue testQueue;

    @BeforeEach
    void setUp() {
        testQueue = KitchenQueue.builder()
                .id(1L)
                .orderId(100L)
                .orderItemId(10L)
                .dishName("Pizza")
                .quantity(2)
                .status(DishStatus.READY)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void publishDishReady_ShouldSendToKafka() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq(KafkaTopics.KITCHEN_DISH_READY), any(String.class), any()))
                .thenReturn(future);

        publisher.publishDishReady(testQueue);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals(KafkaTopics.KITCHEN_DISH_READY, topicCaptor.getValue());
        assertEquals("100", keyCaptor.getValue());
        assertNotNull(eventCaptor.getValue());
        assertTrue(eventCaptor.getValue() instanceof DomainEvent);
    }

    @Test
    void publishDishReady_ShouldUseOrderIdAsKey() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(any(), any(String.class), any())).thenReturn(future);

        KitchenQueue queueWithDifferentOrderId = KitchenQueue.builder()
                .id(2L)
                .orderId(999L)
                .orderItemId(20L)
                .dishName("Burger")
                .quantity(1)
                .status(DishStatus.READY)
                .createdAt(LocalDateTime.now())
                .build();

        publisher.publishDishReady(queueWithDifferentOrderId);

        verify(kafkaTemplate).send(any(), eq("999"), any());
    }
}
