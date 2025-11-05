package ru.ifmo.se.restaurant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.*;
import ru.ifmo.se.restaurant.exception.BusinessException;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.model.OrderStatus;
import ru.ifmo.se.restaurant.repository.EmployeeRepository;
import ru.ifmo.se.restaurant.repository.TableRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest extends BaseIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private MenuManagementService menuService;

    @Autowired
    private TableManagementService tableService;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Long tableId;
    private Long waiterId;
    private Long categoryId;
    private Long dishId;

    @BeforeEach
    void setUp() {
        TableDto table = new TableDto();
        table.setTableNumber(1);
        table.setCapacity(4);
        TableDto createdTable = tableService.createTable(table);
        tableId = createdTable.getId();

        ru.ifmo.se.restaurant.model.entity.Employee employee = new ru.ifmo.se.restaurant.model.entity.Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setRole(ru.ifmo.se.restaurant.model.EmployeeRole.WAITER);
        employee.setIsActive(true);
        ru.ifmo.se.restaurant.model.entity.Employee savedEmployee = employeeRepository.save(employee);
        waiterId = savedEmployee.getId();

        CategoryDto category = new CategoryDto();
        category.setName("Main Course");
        CategoryDto createdCategory = menuService.createCategory(category);
        categoryId = createdCategory.getId();

        DishDto dish = new DishDto();
        dish.setName("Burger");
        dish.setPrice(new BigDecimal("10.00"));
        dish.setCost(new BigDecimal("4.00"));
        dish.setCategoryId(categoryId);
        DishDto createdDish = menuService.createDish(dish);
        dishId = createdDish.getId();
    }

    @Test
    void testCreateOrder() {
        OrderDto order = new OrderDto();
        order.setTableId(tableId);
        order.setWaiterId(waiterId);

        OrderItemDto item = new OrderItemDto();
        item.setDishId(dishId);
        item.setQuantity(2);
        List<OrderItemDto> items = new ArrayList<>();
        items.add(item);
        order.setItems(items);

        OrderDto created = orderService.createOrder(order);
        assertNotNull(created.getId());
        assertEquals(OrderStatus.CREATED, created.getStatus());
        assertTrue(created.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testAddItemToOrder() {
        OrderDto order = new OrderDto();
        order.setTableId(tableId);
        order.setWaiterId(waiterId);
        order.setItems(new ArrayList<>());
        OrderDto created = orderService.createOrder(order);

        OrderItemDto item = new OrderItemDto();
        item.setDishId(dishId);
        item.setQuantity(1);

        OrderDto updated = orderService.addItemToOrder(created.getId(), item);
        assertEquals(1, updated.getItems().size());
    }

    @Test
    void testSendOrderToKitchen() {
        OrderDto order = new OrderDto();
        order.setTableId(tableId);
        order.setWaiterId(waiterId);

        OrderItemDto item = new OrderItemDto();
        item.setDishId(dishId);
        item.setQuantity(1);
        List<OrderItemDto> items = new ArrayList<>();
        items.add(item);
        order.setItems(items);

        OrderDto created = orderService.createOrder(order);
        OrderDto sent = orderService.sendOrderToKitchen(created.getId());
        assertEquals(OrderStatus.IN_KITCHEN, sent.getStatus());
    }

    @Test
    void testSendEmptyOrderToKitchenShouldFail() {
        OrderDto order = new OrderDto();
        order.setTableId(tableId);
        order.setWaiterId(waiterId);
        order.setItems(new ArrayList<>());
        OrderDto created = orderService.createOrder(order);

        assertThrows(BusinessException.class, () -> orderService.sendOrderToKitchen(created.getId()));
    }
}

