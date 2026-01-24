package ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.billing.application.dto.OrderDto;
import ru.ifmo.se.restaurant.billing.domain.exception.OrderServiceException;

@Slf4j
@Component
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public OrderDto getOrder(Long id) {
        log.error("Order service is unavailable for order ID: {}", id);
        throw new OrderServiceException("Order service is currently unavailable");
    }
}
