package ru.ifmo.se.restaurant.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.dto.OrderDto;
import ru.ifmo.se.restaurant.order.dto.OrderItemDto;
import ru.ifmo.se.restaurant.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderDto> createOrder(@Valid @RequestBody OrderDto dto) {
        return orderService.createOrder(dto);
    }

    @GetMapping("/{id}")
    public Mono<OrderDto> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping
    public Flux<OrderDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping("/{id}/items")
    public Mono<OrderDto> addItemToOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderItemDto itemDto) {
        return orderService.addItemToOrder(id, itemDto);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeItemFromOrder(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        return orderService.removeItemFromOrder(id, itemId);
    }

    @PostMapping("/{id}/send-to-kitchen")
    public Mono<OrderDto> sendToKitchen(@PathVariable Long id) {
        return orderService.sendToKitchen(id);
    }

    @PostMapping("/{id}/close")
    public Mono<OrderDto> closeOrder(@PathVariable Long id) {
        return orderService.closeOrder(id);
    }
}
