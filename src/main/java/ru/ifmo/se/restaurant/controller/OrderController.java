package ru.ifmo.se.restaurant.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto dto) {
        return new ResponseEntity<>(orderService.createOrder(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get order by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<OrderDto> getOrder(
            @Parameter(description = "Order ID", required = true, example = "1") @PathVariable @NonNull Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all orders")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size));
    }

    @PostMapping("/{orderId}/items")
    @io.swagger.v3.oas.annotations.Operation(summary = "Add item to order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<OrderDto> addItemToOrder(
            @Parameter(description = "Order ID", required = true, example = "1") @PathVariable Long orderId,
            @Valid @RequestBody OrderItemDto itemDto) {
        return ResponseEntity.ok(orderService.addItemToOrder(orderId, itemDto));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Remove item from order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "No content"),
        @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    public ResponseEntity<Void> removeItemFromOrder(
            @Parameter(description = "Order ID", required = true, example = "1") @PathVariable Long orderId,
            @Parameter(description = "Item ID", required = true, example = "1") @PathVariable Long itemId) {
        orderService.removeItemFromOrder(orderId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/send-to-kitchen")
    @io.swagger.v3.oas.annotations.Operation(summary = "Send order to kitchen")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<OrderDto> sendOrderToKitchen(
            @Parameter(description = "Order ID", required = true, example = "1") @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.sendOrderToKitchen(orderId));
    }

    @PostMapping("/{orderId}/close")
    @io.swagger.v3.oas.annotations.Operation(summary = "Close order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<OrderDto> closeOrder(
            @Parameter(description = "Order ID", required = true, example = "1") @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.closeOrder(orderId));
    }
}

