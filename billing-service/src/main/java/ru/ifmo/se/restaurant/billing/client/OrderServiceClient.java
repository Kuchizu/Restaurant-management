package ru.ifmo.se.restaurant.billing.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.ifmo.se.restaurant.billing.dto.OrderDto;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @GetMapping("/api/orders/{id}")
    @CircuitBreaker(name = "orderService")
    OrderDto getOrder(@PathVariable("id") Long id);
}
