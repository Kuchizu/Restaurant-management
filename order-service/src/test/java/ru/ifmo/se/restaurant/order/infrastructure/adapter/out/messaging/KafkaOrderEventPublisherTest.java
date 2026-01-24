package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.messaging;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import ru.ifmo.se.restaurant.common.event.KafkaTopics;
import ru.ifmo.se.restaurant.order.domain.entity.Order;
import ru.ifmo.se.restaurant.order.domain.entity.OrderItem;
import ru.ifmo.se.restaurant.order.domain.valueobject.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaOrderEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaOrderEventPublisher publisher;

    private Order testOrder;
    private List<OrderItem> testItems;

    @BeforeEach
    void setUp() {
        publisher = new KafkaOrderEventPublisher(kafkaTemplate);

        testOrder = new Order(1L, 10L, 5L, OrderStatus.CREATED,
                new BigDecimal("100.00"), "No onions", LocalDateTime.now(), null, 1L);

        testItems = Arrays.asList(
                new OrderItem(1L, 1L, 100L, "Pizza", 2, new BigDecimal("30.00"), "Extra cheese"),
                new OrderItem(2L, 1L, 101L, "Pasta", 1, new BigDecimal("20.00"), null)
        );
    }

    @Test
    void publishOrderCreated_ShouldSendToKafka() {
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(KafkaTopics.ORDERS_CREATED, 0), 0, 0, 0L, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(
                new ProducerRecord<>(KafkaTopics.ORDERS_CREATED, "1", new Object()), metadata);

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq(KafkaTopics.ORDERS_CREATED), anyString(), any())).thenReturn(future);

        publisher.publishOrderCreated(testOrder, testItems);

        verify(kafkaTemplate).send(eq(KafkaTopics.ORDERS_CREATED), eq("1"), any());
    }

    @Test
    void publishOrderCreated_WithNullCreatedAt_ShouldUseCurrentTime() {
        Order orderWithNullDate = new Order(2L, 10L, 5L, OrderStatus.CREATED,
                new BigDecimal("50.00"), null, null, null, 1L);

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(KafkaTopics.ORDERS_CREATED, 0), 0, 0, 0L, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(
                new ProducerRecord<>(KafkaTopics.ORDERS_CREATED, "2", new Object()), metadata);

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq(KafkaTopics.ORDERS_CREATED), anyString(), any())).thenReturn(future);

        publisher.publishOrderCreated(orderWithNullDate, testItems);

        verify(kafkaTemplate).send(eq(KafkaTopics.ORDERS_CREATED), eq("2"), any());
    }

    @Test
    void publishOrderCreated_WhenKafkaFails_ShouldLogError() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(eq(KafkaTopics.ORDERS_CREATED), anyString(), any())).thenReturn(future);

        publisher.publishOrderCreated(testOrder, testItems);

        verify(kafkaTemplate).send(eq(KafkaTopics.ORDERS_CREATED), eq("1"), any());
    }

    @Test
    void publishOrderSentToKitchen_ShouldSendToKafka() {
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(KafkaTopics.ORDERS_SENT_TO_KITCHEN, 0), 0, 0, 0L, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(
                new ProducerRecord<>(KafkaTopics.ORDERS_SENT_TO_KITCHEN, "1", new Object()), metadata);

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq(KafkaTopics.ORDERS_SENT_TO_KITCHEN), anyString(), any())).thenReturn(future);

        publisher.publishOrderSentToKitchen(testOrder, testItems);

        verify(kafkaTemplate).send(eq(KafkaTopics.ORDERS_SENT_TO_KITCHEN), eq("1"), any());
    }

    @Test
    void publishOrderSentToKitchen_WhenKafkaFails_ShouldLogError() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(eq(KafkaTopics.ORDERS_SENT_TO_KITCHEN), anyString(), any())).thenReturn(future);

        publisher.publishOrderSentToKitchen(testOrder, testItems);

        verify(kafkaTemplate).send(eq(KafkaTopics.ORDERS_SENT_TO_KITCHEN), eq("1"), any());
    }
}
