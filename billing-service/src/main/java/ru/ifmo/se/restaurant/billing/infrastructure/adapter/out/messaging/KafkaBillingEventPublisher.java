package ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.billing.application.port.out.BillingEventPublisher;
import ru.ifmo.se.restaurant.billing.domain.entity.Bill;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.KafkaTopics;
import ru.ifmo.se.restaurant.common.event.billing.BillGeneratedEvent;
import ru.ifmo.se.restaurant.common.event.billing.BillPaidEvent;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaBillingEventPublisher implements BillingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishBillGenerated(Bill bill) {
        BillGeneratedEvent payload = BillGeneratedEvent.builder()
                .billId(bill.getId())
                .orderId(bill.getOrderId())
                .subtotal(bill.getTotalAmount())
                .tax(bill.getTaxAmount())
                .serviceCharge(bill.getServiceCharge())
                .totalAmount(bill.getFinalAmount())
                .generatedAt(Instant.now())
                .build();

        DomainEvent<BillGeneratedEvent> event = DomainEvent.create("BILL_GENERATED", payload);
        String key = String.valueOf(bill.getOrderId());

        kafkaTemplate.send(KafkaTopics.BILLING_GENERATED, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish BILL_GENERATED event for bill: {}", bill.getId(), ex);
                    } else {
                        log.info("Published BILL_GENERATED event for order: {}, bill: {} to partition: {}",
                                bill.getOrderId(),
                                bill.getId(),
                                result.getRecordMetadata().partition());
                    }
                });
    }

    @Override
    public void publishBillPaid(Bill bill) {
        BillPaidEvent payload = BillPaidEvent.builder()
                .billId(bill.getId())
                .orderId(bill.getOrderId())
                .amountPaid(bill.getFinalAmount())
                .paymentMethod(bill.getPaymentMethod() != null ? bill.getPaymentMethod().name() : null)
                .paidAt(Instant.now())
                .build();

        DomainEvent<BillPaidEvent> event = DomainEvent.create("BILL_PAID", payload);
        String key = String.valueOf(bill.getOrderId());

        kafkaTemplate.send(KafkaTopics.BILLING_PAID, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish BILL_PAID event for bill: {}", bill.getId(), ex);
                    } else {
                        log.info("Published BILL_PAID event for order: {}, bill: {} to partition: {}",
                                bill.getOrderId(),
                                bill.getId(),
                                result.getRecordMetadata().partition());
                    }
                });
    }
}
