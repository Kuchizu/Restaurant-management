package ru.ifmo.se.restaurant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.*;
import ru.ifmo.se.restaurant.exception.BusinessException;
import ru.ifmo.se.restaurant.model.OrderStatus;
import ru.ifmo.se.restaurant.model.entity.Employee;
import ru.ifmo.se.restaurant.model.entity.Order;
import ru.ifmo.se.restaurant.repository.EmployeeRepository;
import ru.ifmo.se.restaurant.repository.OrderRepository;
import ru.ifmo.se.restaurant.repository.TableRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BillingServiceTest extends BaseIntegrationTest {
    @Autowired
    private BillingService billingService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MenuManagementService menuService;

    @Autowired
    private TableManagementService tableService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TableRepository tableRepository;

    private Long orderId;
    private Long tableId;
    private Long waiterId;
    private Long categoryId;
    private Long dishId;

    @BeforeEach
    void setUp() {
        TableDto table = new TableDto();
        table.setTableNumber(2);
        table.setCapacity(6);
        TableDto createdTable = tableService.createTable(table);
        tableId = createdTable.getId();

        Employee employee = new Employee();
        employee.setFirstName("Jane");
        employee.setLastName("Smith");
        employee.setRole(ru.ifmo.se.restaurant.model.EmployeeRole.WAITER);
        employee.setIsActive(true);
        Employee savedEmployee = employeeRepository.save(employee);
        waiterId = savedEmployee.getId();

        CategoryDto category = new CategoryDto();
        category.setName("Desserts");
        CategoryDto createdCategory = menuService.createCategory(category);
        categoryId = createdCategory.getId();

        DishDto dish = new DishDto();
        dish.setName("Ice Cream");
        dish.setPrice(new BigDecimal("5.00"));
        dish.setCost(new BigDecimal("2.00"));
        dish.setCategoryId(categoryId);
        DishDto createdDish = menuService.createDish(dish);
        dishId = createdDish.getId();

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
        orderId = created.getId();

        Order orderEntity = orderRepository.findById(orderId).orElseThrow();
        orderEntity.setStatus(OrderStatus.READY);
        orderRepository.save(orderEntity);
    }

    @Test
    void testFinalizeOrder() {
        BillDto bill = billingService.finalizeOrder(orderId, BigDecimal.ZERO, "Test bill");
        assertNotNull(bill.getId());
        assertTrue(bill.getTotal().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testFinalizeOrderWithDiscount() {
        BigDecimal discount = new BigDecimal("1.00");
        BillDto bill = billingService.finalizeOrder(orderId, discount, null);
        assertNotNull(bill.getId());
        assertEquals(discount, bill.getDiscount());
    }

    @Test
    void testFinalizeOrderWithInvalidStatusShouldFail() {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.CREATED);
        orderRepository.save(order);

        assertThrows(BusinessException.class, () -> 
            billingService.finalizeOrder(orderId, BigDecimal.ZERO, null));
    }
}

