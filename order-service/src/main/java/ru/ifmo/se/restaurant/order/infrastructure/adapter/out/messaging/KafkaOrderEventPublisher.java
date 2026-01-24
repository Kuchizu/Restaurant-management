package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.KafkaTopics;
import ru.ifmo.se.restaurant.common.event.order.OrderCreatedEvent;
import ru.ifmo.se.restaurant.common.event.order.OrderSentToKitchenEvent;
import ru.ifmo.se.restaurant.order.application.port.out.OrderEventPublisher;
import ru.ifmo.se.restaurant.order.domain.entity.Order;
import ru.ifmo.se.restaurant.order.domain.entity.OrderItem;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishOrderCreated(Order order, List<OrderItem> items) {
        OrderCreatedEvent payload = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .tableId(order.getTableId())
                .waiterId(order.getWaiterId())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : java.time.Instant.now())
                .totalAmount(order.getTotalAmount())
                .items(items.stream()
                        .map(item -> OrderCreatedEvent.OrderItemData.builder()
                                .itemId(item.getId())
                                .dishId(item.getDishId())
                                .dishName(item.getDishName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build())
                        .toList())
                .build();

        DomainEvent<OrderCreatedEvent> event = DomainEvent.create("ORDER_CREATED", payload);
        String key = String.valueOf(order.getId());

        kafkaTemplate.send(KafkaTopics.ORDERS_CREATED, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish ORDER_CREATED event for order: {}", order.getId(), ex);
                    } else {
                        log.info("Published ORDER_CREATED event for order: {} to partition: {}",
                                order.getId(), result.getRecordMetadata().partition());
                    }
                });
    }

    @Override
    public void publishOrderSentToKitchen(Order order, List<OrderItem> items) {
        OrderSentToKitchenEvent payload = OrderSentToKitchenEvent.builder()
                .orderId(order.getId())
                .tableId(order.getTableId())
                .sentAt(java.time.Instant.now())
                .items(items.stream()
                        .map(item -> OrderSentToKitchenEvent.KitchenItem.builder()
                                .orderItemId(item.getId())
                                .dishId(item.getDishId())
                                .dishName(item.getDishName())
                                .quantity(item.getQuantity())
                                .specialInstructions(item.getSpecialRequest())
                                .build())
                        .toList())
                .build();

        DomainEvent<OrderSentToKitchenEvent> event = DomainEvent.create("ORDER_SENT_TO_KITCHEN", payload);
        String key = String.valueOf(order.getId());

        kafkaTemplate.send(KafkaTopics.ORDERS_SENT_TO_KITCHEN, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish ORDER_SENT_TO_KITCHEN event for order: {}", order.getId(), ex);
                    } else {
                        log.info("Published ORDER_SENT_TO_KITCHEN event for order: {} to partition: {}",
                                order.getId(), result.getRecordMetadata().partition());
                    }
                });
    }
}
