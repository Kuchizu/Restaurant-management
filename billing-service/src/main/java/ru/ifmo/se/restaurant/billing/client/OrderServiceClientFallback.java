package ru.ifmo.se.restaurant.billing.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.billing.dto.OrderDto;
import ru.ifmo.se.restaurant.billing.exception.ServiceUnavailableException;

@Slf4j
@Component
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public OrderDto getOrder(Long id) {
        log.error("Order service is unavailable for order ID: {}", id);
        throw new ServiceUnavailableException(
            "Order service is currently unavailable",
            "order-service",
            "getOrder"
        );
    }
}
