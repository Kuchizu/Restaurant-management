package ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.billing.application.dto.OrderDto;
import ru.ifmo.se.restaurant.billing.application.port.out.OrderServicePort;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceAdapter implements OrderServicePort {
    private final OrderServiceClient orderServiceClient;

    @Override
    public OrderDto getOrder(Long orderId) {
        log.debug("Fetching order with id: {}", orderId);
        return orderServiceClient.getOrder(orderId);
    }
}
