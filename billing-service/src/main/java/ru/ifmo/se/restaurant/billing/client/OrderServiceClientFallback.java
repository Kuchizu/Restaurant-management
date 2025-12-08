package ru.ifmo.se.restaurant.billing.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.billing.dto.OrderDto;

@Slf4j
@Component
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public OrderDto getOrder(Long id) {
        log.warn("Order service is unavailable, returning fallback for order ID: {}", id);
        return null;
    }
}
