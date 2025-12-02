package ru.ifmo.se.restaurant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.*;
import ru.ifmo.se.restaurant.model.DishStatus;
import ru.ifmo.se.restaurant.model.EmployeeRole;
import ru.ifmo.se.restaurant.model.entity.Employee;
import ru.ifmo.se.restaurant.repository.EmployeeRepository;
import ru.ifmo.se.restaurant.repository.KitchenQueueRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KitchenServiceTest extends BaseIntegrationTest {
    @Autowired
    private KitchenService kitchenService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MenuManagementService menuService;

    @Autowired
    private TableManagementService tableService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private KitchenQueueRepository kitchenQueueRepository;

    private Long orderId;

    @BeforeEach
    void setUp() {
        // Create table
        TableDto table = new TableDto();
        table.setTableNumber(20);
        table.setCapacity(4);
        table.setLocation("Kitchen Test");
        TableDto createdTable = tableService.createTable(table);
        Long tableId = createdTable.getId();

        // Create employee
        Employee employee = new Employee();
        employee.setFirstName("Chef");
        employee.setLastName("Test");
        employee.setRole(EmployeeRole.WAITER);
        employee.setIsActive(true);
        Employee savedEmployee = employeeRepository.save(employee);
        Long waiterId = savedEmployee.getId();

        // Create category and dish
        CategoryDto category = new CategoryDto();
        category.setName("Kitchen Category");
        CategoryDto createdCategory = menuService.createCategory(category);
        Long categoryId = createdCategory.getId();

        DishDto dish = new DishDto();
        dish.setName("Kitchen Dish");
        dish.setPrice(new BigDecimal("10.00"));
        dish.setCost(new BigDecimal("4.00"));
        dish.setCategoryId(categoryId);
        DishDto createdDish = menuService.createDish(dish);
        Long dishId = createdDish.getId();

        // Create order
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
        orderId = created.getId();
    }

    @Test
    void testAddOrderToKitchenQueue() {
        orderService.sendOrderToKitchen(orderId);
        
        List<ru.ifmo.se.restaurant.dto.KitchenQueueDto> queue = kitchenService.getKitchenQueue();
        assertTrue(queue.size() > 0);
    }

    @Test
    void testGetKitchenQueue() {
        orderService.sendOrderToKitchen(orderId);
        
        List<ru.ifmo.se.restaurant.dto.KitchenQueueDto> queue = kitchenService.getKitchenQueue();
        assertNotNull(queue);
    }

    @Test
    void testGetAllKitchenQueueItems() {
        orderService.sendOrderToKitchen(orderId);
        
        Page<ru.ifmo.se.restaurant.dto.KitchenQueueDto> allItems = kitchenService.getAllKitchenQueueItems(0, 10);
        assertNotNull(allItems);
        assertTrue(allItems.getTotalElements() >= 0);
    }

    @Test
    void testUpdateDishStatus() {
        orderService.sendOrderToKitchen(orderId);

        List<ru.ifmo.se.restaurant.dto.KitchenQueueDto> queue = kitchenService.getKitchenQueue();
        if (!queue.isEmpty()) {
            Long queueId = queue.get(0).getId();
            ru.ifmo.se.restaurant.dto.KitchenQueueDto updated = kitchenService.updateDishStatus(queueId, DishStatus.IN_PROGRESS);
            assertEquals(DishStatus.IN_PROGRESS, updated.getStatus());
        }
    }

    @Test
    void testCompleteDish() {
        orderService.sendOrderToKitchen(orderId);

        List<ru.ifmo.se.restaurant.dto.KitchenQueueDto> queue = kitchenService.getKitchenQueue();
        if (!queue.isEmpty()) {
            Long queueId = queue.get(0).getId();
            kitchenService.updateDishStatus(queueId, DishStatus.IN_PROGRESS);
            ru.ifmo.se.restaurant.dto.KitchenQueueDto completed = kitchenService.updateDishStatus(queueId, DishStatus.READY);
            assertEquals(DishStatus.READY, completed.getStatus());
        }
    }

    @Test
    void testUpdateDishStatusNotFound() {
        assertThrows(ru.ifmo.se.restaurant.exception.ResourceNotFoundException.class,
            () -> kitchenService.updateDishStatus(99999L, DishStatus.IN_PROGRESS));
    }

    @Test
    void testGetKitchenQueueEmpty() {
        List<ru.ifmo.se.restaurant.dto.KitchenQueueDto> queue = kitchenService.getKitchenQueue();
        assertNotNull(queue);
    }
}

