package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.KafkaTopics;
import ru.ifmo.se.restaurant.common.event.kitchen.DishReadyEvent;
import ru.ifmo.se.restaurant.kitchen.application.port.out.KitchenEventPublisher;
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaKitchenEventPublisher implements KitchenEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishDishReady(KitchenQueue kitchenQueue) {
        DishReadyEvent payload = DishReadyEvent.builder()
                .kitchenQueueId(kitchenQueue.getId())
                .orderId(kitchenQueue.getOrderId())
                .orderItemId(kitchenQueue.getOrderItemId())
                .dishName(kitchenQueue.getDishName())
                .quantity(kitchenQueue.getQuantity())
                .readyAt(Instant.now())
                .build();

        DomainEvent<DishReadyEvent> event = DomainEvent.create("DISH_READY", payload);
        String key = String.valueOf(kitchenQueue.getOrderId());

        kafkaTemplate.send(KafkaTopics.KITCHEN_DISH_READY, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish DISH_READY event for kitchen queue: {}", kitchenQueue.getId(), ex);
                    } else {
                        log.info("Published DISH_READY event for order: {}, dish: {} to partition: {}",
                                kitchenQueue.getOrderId(),
                                kitchenQueue.getDishName(),
                                result.getRecordMetadata().partition());
                    }
                });
    }
}
