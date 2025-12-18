package ru.ifmo.se.restaurant.order;

import ru.ifmo.se.restaurant.order.dto.DishResponse;
import ru.ifmo.se.restaurant.order.dto.OrderDto;
import ru.ifmo.se.restaurant.order.dto.OrderItemDto;
import ru.ifmo.se.restaurant.order.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {

    public static Employee createEmployee(Long id, String firstName, String lastName, EmployeeRole role) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@restaurant.com");
        employee.setPhone("+1234567890");
        employee.setRole(role);
        return employee;
    }

    public static Employee createDefaultWaiter() {
        return createEmployee(1L, "John", "Doe", EmployeeRole.WAITER);
    }

    public static RestaurantTable createTable(Long id, String tableNumber, Integer capacity, TableStatus status) {
        RestaurantTable table = new RestaurantTable();
        table.setId(id);
        table.setTableNumber(tableNumber);
        table.setCapacity(capacity);
        table.setLocation("Main Hall");
        table.setStatus(status);
        return table;
    }

    public static RestaurantTable createDefaultTable() {
        return createTable(1L, "T-01", 4, TableStatus.FREE);
    }

    public static Order createOrder(Long id, Long tableId, Long waiterId, OrderStatus status, BigDecimal totalAmount) {
        Order order = new Order();
        order.setId(id);
        order.setTableId(tableId);
        order.setWaiterId(waiterId);
        order.setStatus(status);
        order.setTotalAmount(totalAmount);
        order.setSpecialRequests(null);
        order.setCreatedAt(LocalDateTime.now());
        order.setClosedAt(null);
        return order;
    }

    public static Order createDefaultOrder() {
        return createOrder(1L, 1L, 1L, OrderStatus.CREATED, BigDecimal.ZERO);
    }

    public static OrderItem createOrderItem(Long id, Long orderId, Long dishId, String dishName,
                                           Integer quantity, BigDecimal price) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setOrderId(orderId);
        item.setDishId(dishId);
        item.setDishName(dishName);
        item.setQuantity(quantity);
        item.setPrice(price);
        item.setSpecialRequest(null);
        return item;
    }

    public static OrderItem createDefaultOrderItem() {
        return createOrderItem(1L, 1L, 1L, "Pasta Carbonara", 2, new BigDecimal("12.50"));
    }

    public static OrderDto createOrderDto(Long tableId, Long waiterId, String specialRequests) {
        OrderDto dto = new OrderDto();
        dto.setTableId(tableId);
        dto.setWaiterId(waiterId);
        dto.setSpecialRequests(specialRequests);
        dto.setStatus(OrderStatus.CREATED);
        dto.setTotalAmount(BigDecimal.ZERO);
        dto.setItems(new ArrayList<>());
        return dto;
    }

    public static OrderDto createDefaultOrderDto() {
        return createOrderDto(1L, 1L, null);
    }

    public static OrderDto createCompleteOrderDto(Long id, Long tableId, Long waiterId,
                                                  OrderStatus status, BigDecimal totalAmount,
                                                  List<OrderItemDto> items) {
        OrderDto dto = new OrderDto();
        dto.setId(id);
        dto.setTableId(tableId);
        dto.setWaiterId(waiterId);
        dto.setStatus(status);
        dto.setTotalAmount(totalAmount);
        dto.setItems(items);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }

    public static OrderItemDto createOrderItemDto(Long dishId, Integer quantity, String specialRequest) {
        OrderItemDto dto = new OrderItemDto();
        dto.setDishId(dishId);
        dto.setQuantity(quantity);
        dto.setSpecialRequest(specialRequest);
        return dto;
    }

    public static OrderItemDto createDefaultOrderItemDto() {
        return createOrderItemDto(1L, 2, null);
    }

    public static DishResponse createDishResponse(Long id, String name, BigDecimal price, Boolean isActive) {
        DishResponse response = new DishResponse();
        response.setId(id);
        response.setName(name);
        response.setPrice(price);
        response.setIsActive(isActive);
        return response;
    }

    public static DishResponse createDefaultDishResponse() {
        return createDishResponse(1L, "Pasta Carbonara", new BigDecimal("12.50"), true);
    }
}
