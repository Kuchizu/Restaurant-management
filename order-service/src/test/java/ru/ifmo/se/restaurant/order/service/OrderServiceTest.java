package ru.ifmo.se.restaurant.order.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.ifmo.se.restaurant.order.client.KitchenServiceClient;
import ru.ifmo.se.restaurant.order.client.MenuServiceClient;
import ru.ifmo.se.restaurant.order.dataaccess.EmployeeDataAccess;
import ru.ifmo.se.restaurant.order.dataaccess.OrderDataAccess;
import ru.ifmo.se.restaurant.order.dataaccess.OrderItemDataAccess;
import ru.ifmo.se.restaurant.order.dataaccess.TableDataAccess;
import ru.ifmo.se.restaurant.order.dto.DishResponse;
import ru.ifmo.se.restaurant.order.dto.OrderDto;
import ru.ifmo.se.restaurant.order.dto.OrderItemDto;
import ru.ifmo.se.restaurant.order.entity.*;
import ru.ifmo.se.restaurant.order.exception.BusinessConflictException;
import ru.ifmo.se.restaurant.order.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.exception.ServiceUnavailableException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.order.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderDataAccess orderDataAccess;

    @Mock
    private OrderItemDataAccess orderItemDataAccess;

    @Mock
    private TableDataAccess tableDataAccess;

    @Mock
    private EmployeeDataAccess employeeDataAccess;

    @Mock
    private KitchenServiceClient kitchenServiceClient;

    @Mock
    private MenuServiceClient menuServiceClient;

    @InjectMocks
    private OrderService orderService;

    // ========== CREATE ORDER TESTS ==========

    @Test
    void createOrder_WhenTableFreeAndWaiterExists_CreatesOrderSuccessfully() {
        // Given
        OrderDto inputDto = createDefaultOrderDto();
        RestaurantTable table = createDefaultTable();
        Employee waiter = createDefaultWaiter();
        Order savedOrder = createDefaultOrder();
        savedOrder.setId(1L);

        when(tableDataAccess.getById(1L)).thenReturn(Mono.just(table));
        when(employeeDataAccess.getById(1L)).thenReturn(Mono.just(waiter));
        when(orderDataAccess.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
        when(tableDataAccess.save(any(RestaurantTable.class))).thenReturn(Mono.just(table));
        when(orderItemDataAccess.findByOrderId(1L)).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(orderService.createOrder(inputDto))
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                                dto.getStatus() == OrderStatus.CREATED &&
                                dto.getTotalAmount().compareTo(BigDecimal.ZERO) == 0
                )
                .verifyComplete();

        verify(tableDataAccess).save(argThat(t -> t.getStatus() == TableStatus.OCCUPIED));
    }

    @Test
    void createOrder_WhenTableOccupied_ThrowsBusinessConflictException() {
        // Given
        OrderDto inputDto = createDefaultOrderDto();
        RestaurantTable occupiedTable = createTable(1L, "T-01", 4, TableStatus.OCCUPIED);

        when(tableDataAccess.getById(1L)).thenReturn(Mono.just(occupiedTable));

        // When & Then
        StepVerifier.create(orderService.createOrder(inputDto))
                .expectError(BusinessConflictException.class)
                .verify();

        verify(employeeDataAccess, never()).getById(any());
        verify(orderDataAccess, never()).save(any());
    }

    @Test
    void createOrder_WhenTableNotFound_ThrowsResourceNotFoundException() {
        // Given
        OrderDto inputDto = createDefaultOrderDto();

        when(tableDataAccess.getById(1L))
                .thenReturn(Mono.error(new ResourceNotFoundException("Table not found")));

        // When & Then
        StepVerifier.create(orderService.createOrder(inputDto))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(employeeDataAccess, never()).getById(any());
    }

    @Test
    void createOrder_WhenWaiterNotFound_ThrowsResourceNotFoundException() {
        // Given
        OrderDto inputDto = createDefaultOrderDto();
        RestaurantTable table = createDefaultTable();

        when(tableDataAccess.getById(1L)).thenReturn(Mono.just(table));
        when(employeeDataAccess.getById(1L))
                .thenReturn(Mono.error(new ResourceNotFoundException("Employee not found")));

        // When & Then
        StepVerifier.create(orderService.createOrder(inputDto))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(orderDataAccess, never()).save(any());
    }

    // ========== GET ORDER TESTS ==========

    @Test
    void getOrderById_WhenExists_ReturnsOrderDto() {
        // Given
        Order order = createDefaultOrder();
        order.setId(1L);

        when(orderDataAccess.getById(1L)).thenReturn(Mono.just(order));
        when(orderItemDataAccess.findByOrderId(1L)).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(orderService.getOrderById(1L))
                .expectNextMatches(dto -> dto.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void getOrderById_WhenNotExists_ThrowsResourceNotFoundException() {
        // Given
        when(orderDataAccess.getById(999L))
                .thenReturn(Mono.error(new ResourceNotFoundException("Order not found")));

        // When & Then
        StepVerifier.create(orderService.getOrderById(999L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void getAllOrders_ReturnsAllOrderDtos() {
        // Given
        Order order1 = createOrder(1L, 1L, 1L, OrderStatus.CREATED, BigDecimal.ZERO);
        Order order2 = createOrder(2L, 2L, 1L, OrderStatus.IN_KITCHEN, new BigDecimal("25.00"));

        when(orderDataAccess.findAll()).thenReturn(Flux.just(order1, order2));
        when(orderItemDataAccess.findByOrderId(1L)).thenReturn(Flux.empty());
        when(orderItemDataAccess.findByOrderId(2L)).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(orderService.getAllOrders())
                .expectNextCount(2)
                .verifyComplete();
    }

    // ========== ADD ITEM TO ORDER TESTS ==========

    @Test
    void addItemToOrder_WhenValidDish_AddsItemAndUpdatesTotal() {
        // Given
        Order order = createDefaultOrder();
        order.setId(1L);
        OrderItemDto itemDto = createDefaultOrderItemDto();
        DishResponse dish = createDefaultDishResponse();
        OrderItem savedItem = createDefaultOrderItem();

        when(orderDataAccess.getById(1L)).thenReturn(Mono.just(order));
        when(menuServiceClient.getDish(1L)).thenReturn(Mono.just(dish));
        when(orderItemDataAccess.save(any(OrderItem.class))).thenReturn(Mono.just(savedItem));
        when(orderItemDataAccess.findByOrderId(1L)).thenReturn(Flux.just(savedItem));
        when(orderDataAccess.save(any(Order.class))).thenReturn(Mono.just(order));

        // When & Then
        StepVerifier.create(orderService.addItemToOrder(1L, itemDto))
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                                dto.getTotalAmount().compareTo(new BigDecimal("25.00")) == 0
                )
                .verifyComplete();

        verify(menuServiceClient).getDish(1L);
        verify(orderItemDataAccess).save(any(OrderItem.class));
    }

    @Test
    void addItemToOrder_WhenDishNotFound_ThrowsServiceUnavailableException() {
        // Given
        Order order = createDefaultOrder();
        OrderItemDto itemDto = createDefaultOrderItemDto();

        when(orderDataAccess.getById(1L)).thenReturn(Mono.just(order));
        when(menuServiceClient.getDish(1L))
                .thenReturn(Mono.error(new RuntimeException("Dish not found")));

        // When & Then
        StepVerifier.create(orderService.addItemToOrder(1L, itemDto))
                .expectError(RuntimeException.class)
                .verify();

        verify(orderItemDataAccess, never()).save(any());
    }

    @Test
    void addItemToOrder_WhenDishPriceIsNull_ThrowsServiceUnavailableException() {
        // Given
        Order order = createDefaultOrder();
        OrderItemDto itemDto = createDefaultOrderItemDto();
        DishResponse dish = createDishResponse(1L, "Test Dish", null, true);

        when(orderDataAccess.getById(1L)).thenReturn(Mono.just(order));
        when(menuServiceClient.getDish(1L)).thenReturn(Mono.just(dish));

        // When & Then
        StepVerifier.create(orderService.addItemToOrder(1L, itemDto))
                .expectError(ServiceUnavailableException.class)
                .verify();

        verify(orderItemDataAccess, never()).save(any());
    }

    // ========== REMOVE ITEM FROM ORDER TESTS ==========

    @Test
    void removeItemFromOrder_WhenItemExists_RemovesItemAndUpdatesTotal() {
        // Given
        Order order = createDefaultOrder();
        order.setId(1L);
        order.setTotalAmount(new BigDecimal("25.00"));
        OrderItem item = createDefaultOrderItem();

        when(orderDataAccess.getById(1L)).thenReturn(Mono.just(order));
        when(orderItemDataAccess.getById(1L)).thenReturn(Mono.just(item));
        when(orderDataAccess.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemDataAccess.deleteById(1L)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(orderService.removeItemFromOrder(1L, 1L))
                .verifyComplete();

        verify(orderDataAccess).save(any(Order.class));
        verify(orderItemDataAccess).deleteById(1L);
    }

    @Test
    void removeItemFromOrder_WhenItemNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(orderDataAccess.getById(1L)).thenReturn(Mono.just(createDefaultOrder()));
        when(orderItemDataAccess.getById(999L))
                .thenReturn(Mono.error(new ResourceNotFoundException("Item not found")));

        // When & Then
        StepVerifier.create(orderService.removeItemFromOrder(1L, 999L))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(orderItemDataAccess, never()).deleteById(any());
    }

    // ========== SEND TO KITCHEN TESTS ==========

    @Test
    void sendToKitchen_WhenOrderInCreatedStatus_SendsToKitchen() {
        // Given
        Order order = createDefaultOrder();
        order.setId(1L);
        OrderItem item = createDefaultOrderItem();

        when(orderDataAccess.getById(1L)).thenReturn(Mono.just(order));
        when(orderDataAccess.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemDataAccess.findByOrderId(1L)).thenReturn(Flux.just(item));
        when(kitchenServiceClient.addToKitchenQueue(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(orderService.sendToKitchen(1L))
                .expectNextMatches(dto -> dto.getStatus() == OrderStatus.IN_KITCHEN)
                .verifyComplete();

        verify(orderDataAccess).save(argThat(o -> o.getStatus() == OrderStatus.IN_KITCHEN));
        verify(kitchenServiceClient).addToKitchenQueue(any());
    }

    @Test
    void sendToKitchen_WhenOrderNotInCreatedStatus_ThrowsBusinessConflictException() {
        // Given
        Order order = createOrder(1L, 1L, 1L, OrderStatus.CLOSED, BigDecimal.ZERO);

        when(orderDataAccess.getById(1L)).thenReturn(Mono.just(order));

        // When & Then
        StepVerifier.create(orderService.sendToKitchen(1L))
                .expectError(BusinessConflictException.class)
                .verify();

        verify(kitchenServiceClient, never()).addToKitchenQueue(any());
    }

    @Test
    void sendToKitchen_WhenKitchenServiceUnavailable_ThrowsServiceUnavailableException() {
        // Given
        Order order = createDefaultOrder();
        order.setId(1L);
        OrderItem item = createDefaultOrderItem();

        when(orderDataAccess.getById(1L)).thenReturn(Mono.just(order));
        when(orderDataAccess.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemDataAccess.findByOrderId(1L)).thenReturn(Flux.just(item));
        when(kitchenServiceClient.addToKitchenQueue(any()))
                .thenReturn(Mono.error(new ServiceUnavailableException("Kitchen service unavailable", "kitchen-service", "addToQueue")));

        // When & Then
        StepVerifier.create(orderService.sendToKitchen(1L))
                .expectError(ServiceUnavailableException.class)
                .verify();

        verify(orderDataAccess).save(any(Order.class));
    }

    // ========== CLOSE ORDER TESTS ==========

    @Test
    void closeOrder_WhenOrderExists_ClosesOrderAndFreesTable() {
        // Given
        Order order = createOrder(1L, 1L, 1L, OrderStatus.IN_KITCHEN, new BigDecimal("50.00"));
        RestaurantTable table = createTable(1L, "T-01", 4, TableStatus.OCCUPIED);

        when(orderDataAccess.getById(1L)).thenReturn(Mono.just(order));
        when(orderDataAccess.save(any(Order.class))).thenReturn(Mono.just(order));
        when(tableDataAccess.findById(1L)).thenReturn(Mono.just(table));
        when(tableDataAccess.save(any(RestaurantTable.class))).thenReturn(Mono.just(table));
        when(orderItemDataAccess.findByOrderId(1L)).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(orderService.closeOrder(1L))
                .expectNextMatches(dto ->
                        dto.getStatus() == OrderStatus.CLOSED &&
                                dto.getClosedAt() != null
                )
                .verifyComplete();

        verify(orderDataAccess).save(argThat(o -> o.getStatus() == OrderStatus.CLOSED));
        verify(tableDataAccess).save(argThat(t -> t.getStatus() == TableStatus.FREE));
    }
}
