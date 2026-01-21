package ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import ru.ifmo.se.restaurant.billing.domain.entity.Bill;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;
import ru.ifmo.se.restaurant.billing.domain.valueobject.PaymentMethod;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.KafkaTopics;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaBillingEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaBillingEventPublisher publisher;

    private Bill testBill;

    @BeforeEach
    void setUp() {
        testBill = Bill.builder()
                .id(1L)
                .orderId(100L)
                .totalAmount(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .serviceCharge(new BigDecimal("5.00"))
                .finalAmount(new BigDecimal("115.00"))
                .status(BillStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void publishBillGenerated_ShouldSendToKafka() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq(KafkaTopics.BILLING_GENERATED), any(String.class), any()))
                .thenReturn(future);

        publisher.publishBillGenerated(testBill);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals(KafkaTopics.BILLING_GENERATED, topicCaptor.getValue());
        assertEquals("100", keyCaptor.getValue());
        assertNotNull(eventCaptor.getValue());
        assertTrue(eventCaptor.getValue() instanceof DomainEvent);
    }

    @Test
    void publishBillPaid_ShouldSendToKafka() {
        testBill = Bill.builder()
                .id(1L)
                .orderId(100L)
                .totalAmount(new BigDecimal("100.00"))
                .finalAmount(new BigDecimal("115.00"))
                .status(BillStatus.PAID)
                .paymentMethod(PaymentMethod.CASH)
                .createdAt(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq(KafkaTopics.BILLING_PAID), any(String.class), any()))
                .thenReturn(future);

        publisher.publishBillPaid(testBill);

        verify(kafkaTemplate).send(eq(KafkaTopics.BILLING_PAID), eq("100"), any());
    }

    @Test
    void publishBillPaid_ShouldHandleNullPaymentMethod() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq(KafkaTopics.BILLING_PAID), any(String.class), any()))
                .thenReturn(future);

        publisher.publishBillPaid(testBill);

        verify(kafkaTemplate).send(eq(KafkaTopics.BILLING_PAID), any(String.class), any());
    }
}
