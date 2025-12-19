package ru.ifmo.se.restaurant.order.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.dto.EmployeeDto;
import ru.ifmo.se.restaurant.order.dto.OrderDto;
import ru.ifmo.se.restaurant.order.dto.OrderItemDto;
import ru.ifmo.se.restaurant.order.dto.TableDto;
import ru.ifmo.se.restaurant.order.entity.OrderStatus;
import ru.ifmo.se.restaurant.order.exception.BusinessConflictException;
import ru.ifmo.se.restaurant.order.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.service.OrderService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    private OrderDto createDefaultOrderDto() {
        OrderDto dto = new OrderDto();
        dto.setTableId(1L);
        dto.setWaiterId(1L);
        dto.setStatus(OrderStatus.CREATED);
        dto.setTotalAmount(BigDecimal.valueOf(100.00));
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }

    private OrderItemDto createDefaultOrderItemDto() {
        OrderItemDto dto = new OrderItemDto();
        dto.setDishId(1L);
        dto.setQuantity(2);
        dto.setPrice(BigDecimal.valueOf(50.00));
        return dto;
    }

    @Test
    void createOrder_ReturnsCreated() {
        // Given
        OrderDto inputDto = createDefaultOrderDto();
        OrderDto savedDto = createDefaultOrderDto();
        savedDto.setId(1L);

        when(orderService.createOrder(any(OrderDto.class)))
                .thenReturn(Mono.just(savedDto));

        // When & Then
        webTestClient.post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inputDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderDto.class);
    }

    @Test
    void getOrderById_WhenExists_ReturnsOk() {
        // Given
        OrderDto orderDto = createDefaultOrderDto();
        orderDto.setId(1L);

        when(orderService.getOrderById(1L))
                .thenReturn(Mono.just(orderDto));

        // When & Then
        webTestClient.get()
                .uri("/api/orders/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderDto.class);
    }

    @Test
    void getOrderById_WhenNotExists_ReturnsNotFound() {
        // Given
        when(orderService.getOrderById(999L))
                .thenReturn(Mono.error(new ResourceNotFoundException("Order not found")));

        // When & Then
        webTestClient.get()
                .uri("/api/orders/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAllOrders_ReturnsAllOrders() {
        // Given
        OrderDto order1 = createDefaultOrderDto();
        order1.setId(1L);
        OrderDto order2 = createDefaultOrderDto();
        order2.setId(2L);

        when(orderService.getAllOrders())
                .thenReturn(Flux.just(order1, order2));

        // When & Then
        webTestClient.get()
                .uri("/api/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderDto.class)
                .hasSize(2);
    }

    @Test
    void addItemToOrder_ReturnsUpdatedOrder() {
        // Given
        OrderItemDto itemDto = createDefaultOrderItemDto();
        OrderDto updatedOrder = createDefaultOrderDto();
        updatedOrder.setId(1L);

        when(orderService.addItemToOrder(eq(1L), any(OrderItemDto.class)))
                .thenReturn(Mono.just(updatedOrder));

        // When & Then
        webTestClient.post()
                .uri("/api/orders/1/items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(itemDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderDto.class);
    }

    @Test
    void removeItemFromOrder_ReturnsNoContent() {
        // Given
        when(orderService.removeItemFromOrder(1L, 1L))
                .thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/api/orders/1/items/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void sendToKitchen_ReturnsUpdatedOrder() {
        // Given
        OrderDto orderDto = createDefaultOrderDto();
        orderDto.setId(1L);
        orderDto.setStatus(OrderStatus.IN_KITCHEN);

        when(orderService.sendToKitchen(1L))
                .thenReturn(Mono.just(orderDto));

        // When & Then
        webTestClient.post()
                .uri("/api/orders/1/send-to-kitchen")
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderDto.class);
    }

    @Test
    void sendToKitchen_WhenInvalidStatus_ReturnsConflict() {
        // Given
        when(orderService.sendToKitchen(1L))
                .thenReturn(Mono.error(new BusinessConflictException(
                        "Cannot send to kitchen", "Order", 1L, "Invalid status"
                )));

        // When & Then
        webTestClient.post()
                .uri("/api/orders/1/send-to-kitchen")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void closeOrder_ReturnsClosedOrder() {
        // Given
        OrderDto orderDto = createDefaultOrderDto();
        orderDto.setId(1L);
        orderDto.setStatus(OrderStatus.CLOSED);

        when(orderService.closeOrder(1L))
                .thenReturn(Mono.just(orderDto));

        // When & Then
        webTestClient.post()
                .uri("/api/orders/1/close")
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderDto.class);
    }

    @Test
    void getAllOrders_WhenEmpty_ReturnsEmptyList() {
        // Given
        when(orderService.getAllOrders())
                .thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderDto.class)
                .hasSize(0);
    }

    @Test
    void createOrder_WithInvalidData_ReturnsBadRequest() {
        // When & Then
        webTestClient.post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void addItemToOrder_WhenOrderNotFound_ReturnsNotFound() {
        // Given
        OrderItemDto itemDto = createDefaultOrderItemDto();

        when(orderService.addItemToOrder(eq(999L), any(OrderItemDto.class)))
                .thenReturn(Mono.error(new ResourceNotFoundException("Order not found")));

        // When & Then
        webTestClient.post()
                .uri("/api/orders/999/items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(itemDto)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void closeOrder_WhenNotFound_ReturnsNotFound() {
        // Given
        when(orderService.closeOrder(999L))
                .thenReturn(Mono.error(new ResourceNotFoundException("Order not found")));

        // When & Then
        webTestClient.post()
                .uri("/api/orders/999/close")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createOrder_WithDifferentTableId_ReturnsCreated() {
        // Given
        OrderDto inputDto = createDefaultOrderDto();
        inputDto.setTableId(5L);
        OrderDto savedDto = createDefaultOrderDto();
        savedDto.setId(2L);
        savedDto.setTableId(5L);

        when(orderService.createOrder(any(OrderDto.class)))
                .thenReturn(Mono.just(savedDto));

        // When & Then
        webTestClient.post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inputDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderDto.class);
    }

    @Test
    void removeItemFromOrder_WhenNotFound_ReturnsNoContent() {
        // Given
        when(orderService.removeItemFromOrder(1L, 999L))
                .thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/api/orders/1/items/999")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void sendToKitchen_WithMultipleOrders_ReturnsOk() {
        // Given
        OrderDto orderDto = createDefaultOrderDto();
        orderDto.setId(5L);
        orderDto.setStatus(OrderStatus.IN_KITCHEN);

        when(orderService.sendToKitchen(5L))
                .thenReturn(Mono.just(orderDto));

        // When & Then
        webTestClient.post()
                .uri("/api/orders/5/send-to-kitchen")
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderDto.class);
    }

    @Test
    void getOrderById_WithDifferentStatus_ReturnsOk() {
        // Given
        OrderDto orderDto = createDefaultOrderDto();
        orderDto.setId(3L);
        orderDto.setStatus(OrderStatus.IN_KITCHEN);

        when(orderService.getOrderById(3L))
                .thenReturn(Mono.just(orderDto));

        // When & Then
        webTestClient.get()
                .uri("/api/orders/3")
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderDto.class);
    }

    @Test
    void addItemToOrder_WithQuantityOfOne_ReturnsUpdatedOrder() {
        // Given
        OrderItemDto itemDto = createDefaultOrderItemDto();
        itemDto.setQuantity(1);
        OrderDto updatedOrder = createDefaultOrderDto();
        updatedOrder.setId(2L);

        when(orderService.addItemToOrder(eq(2L), any(OrderItemDto.class)))
                .thenReturn(Mono.just(updatedOrder));

        // When & Then
        webTestClient.post()
                .uri("/api/orders/2/items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(itemDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderDto.class);
    }
}
