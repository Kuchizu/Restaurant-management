package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.KafkaTopics;
import ru.ifmo.se.restaurant.common.event.inventory.LowStockEvent;
import ru.ifmo.se.restaurant.inventory.application.port.out.InventoryEventPublisher;
import ru.ifmo.se.restaurant.inventory.domain.entity.Inventory;

import java.time.Instant;

/**
 * Kafka implementation of InventoryEventPublisher.
 * Publishes inventory domain events to Kafka topics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaInventoryEventPublisher implements InventoryEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publishLowStock(Inventory inventory) {
        try {
            LowStockEvent payload = LowStockEvent.builder()
                    .inventoryId(inventory.getId())
                    .ingredientId(inventory.getIngredient().getId())
                    .ingredientName(inventory.getIngredient().getName())
                    .currentQuantity(inventory.getQuantity())
                    .minimumQuantity(inventory.getMinQuantity())
                    .unit(inventory.getIngredient().getUnit())
                    .detectedAt(Instant.now())
                    .build();

            DomainEvent<LowStockEvent> event = DomainEvent.create("LOW_STOCK", payload);

            String message = objectMapper.writeValueAsString(event);
            String key = inventory.getId().toString();

            kafkaTemplate.send(KafkaTopics.INVENTORY_LOW_STOCK, key, message)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish LOW_STOCK event for inventory {}: {}",
                                    inventory.getId(), ex.getMessage(), ex);
                        } else {
                            log.info("Published LOW_STOCK event for inventory {} (ingredient: {}), " +
                                            "current: {}, minimum: {}",
                                    inventory.getId(),
                                    inventory.getIngredient().getName(),
                                    inventory.getQuantity(),
                                    inventory.getMinQuantity());
                        }
                    });

        } catch (Exception e) {
            log.error("Error serializing LOW_STOCK event for inventory {}: {}",
                    inventory.getId(), e.getMessage(), e);
        }
    }
}
