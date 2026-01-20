package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.KafkaTopics;
import ru.ifmo.se.restaurant.common.event.order.OrderSentToKitchenEvent;
import ru.ifmo.se.restaurant.kitchen.application.port.out.KitchenQueueRepository;
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSentToKitchenConsumer {

    private final KitchenQueueRepository kitchenQueueRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopics.ORDERS_SENT_TO_KITCHEN,
            groupId = "${spring.kafka.consumer.group-id:kitchen-service-group}"
    )
    public void handleOrderSentToKitchen(String message) {
        try {
            @SuppressWarnings("unchecked")
            DomainEvent<OrderSentToKitchenEvent> event = objectMapper.readValue(message,
                    objectMapper.getTypeFactory().constructParametricType(DomainEvent.class, OrderSentToKitchenEvent.class));

            OrderSentToKitchenEvent payload = event.getPayload();
            log.info("Received ORDER_SENT_TO_KITCHEN event for order: {} with {} items",
                    payload.getOrderId(), payload.getItems().size());

            for (OrderSentToKitchenEvent.KitchenItem item : payload.getItems()) {
                KitchenQueue queueItem = KitchenQueue.builder()
                        .orderId(payload.getOrderId())
                        .orderItemId(item.getOrderItemId())
                        .dishName(item.getDishName())
                        .quantity(item.getQuantity())
                        .specialRequest(item.getSpecialInstructions())
                        .status(DishStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build();

                KitchenQueue saved = kitchenQueueRepository.save(queueItem);
                log.info("Added dish to kitchen queue: {} for order: {}", saved.getDishName(), saved.getOrderId());
            }

        } catch (Exception e) {
            log.error("Error processing ORDER_SENT_TO_KITCHEN event: {}", e.getMessage(), e);
        }
    }
}
