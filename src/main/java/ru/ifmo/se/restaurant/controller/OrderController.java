package ru.ifmo.se.restaurant.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ifmo.se.restaurant.dto.OrderDto;
import ru.ifmo.se.restaurant.dto.OrderItemDto;
import ru.ifmo.se.restaurant.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Order Management", description = "API for managing restaurant orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new order")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto dto) {
        return new ResponseEntity<>(orderService.createOrder(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get order by ID")
    public ResponseEntity<OrderDto> getOrder(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all orders - infinite scroll (no total count)")
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size));
    }

    @PostMapping("/{orderId}/items")
    @io.swagger.v3.oas.annotations.Operation(summary = "Add item to order")
    public ResponseEntity<OrderDto> addItemToOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderItemDto itemDto) {
        return ResponseEntity.ok(orderService.addItemToOrder(orderId, itemDto));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Remove item from order")
    public ResponseEntity<Void> removeItemFromOrder(
            @PathVariable Long orderId,
            @PathVariable Long itemId) {
        orderService.removeItemFromOrder(orderId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/send-to-kitchen")
    @io.swagger.v3.oas.annotations.Operation(summary = "Send order to kitchen")
    public ResponseEntity<OrderDto> sendOrderToKitchen(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.sendOrderToKitchen(orderId));
    }

    @PostMapping("/{orderId}/close")
    @io.swagger.v3.oas.annotations.Operation(summary = "Close order")
    public ResponseEntity<OrderDto> closeOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.closeOrder(orderId));
    }
}

