package ru.ifmo.se.restaurant.order.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.KafkaTopics;
import ru.ifmo.se.restaurant.common.event.kitchen.DishReadyEvent;
import ru.ifmo.se.restaurant.order.application.port.out.OrderRepositoryPort;
import ru.ifmo.se.restaurant.order.domain.valueobject.OrderStatus;

@Slf4j
@Component
@RequiredArgsConstructor
public class DishReadyEventConsumer {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopics.KITCHEN_DISH_READY,
            groupId = "${spring.kafka.consumer.group-id:order-service-group}"
    )
    public void handleDishReady(String message) {
        try {
            @SuppressWarnings("unchecked")
            DomainEvent<DishReadyEvent> event = objectMapper.readValue(message,
                    objectMapper.getTypeFactory().constructParametricType(DomainEvent.class, DishReadyEvent.class));

            DishReadyEvent payload = event.getPayload();
            log.info("Received DISH_READY event for order: {}, dish: {}",
                    payload.getOrderId(), payload.getDishName());

            orderRepositoryPort.getById(payload.getOrderId())
                    .flatMap(order -> {
                        if (order.getStatus() == OrderStatus.IN_KITCHEN) {
                            order.setStatus(OrderStatus.READY);
                            return orderRepositoryPort.save(order);
                        }
                        return reactor.core.publisher.Mono.just(order);
                    })
                    .subscribe(
                            order -> log.info("Order {} status updated after dish ready", order.getId()),
                            error -> log.error("Failed to update order status", error)
                    );

        } catch (Exception e) {
            log.error("Error processing DISH_READY event: {}", e.getMessage(), e);
        }
    }
}
