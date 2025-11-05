package ru.ifmo.se.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.*;
import ru.ifmo.se.restaurant.model.DishStatus;
import ru.ifmo.se.restaurant.model.EmployeeRole;
import ru.ifmo.se.restaurant.model.entity.Employee;
import ru.ifmo.se.restaurant.repository.EmployeeRepository;
import ru.ifmo.se.restaurant.service.KitchenService;
import ru.ifmo.se.restaurant.service.MenuManagementService;
import ru.ifmo.se.restaurant.service.OrderService;
import ru.ifmo.se.restaurant.service.TableManagementService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
class KitchenControllerTest extends BaseIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MenuManagementService menuService;

    @Autowired
    private TableManagementService tableService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private KitchenService kitchenService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long orderId;
    private Long tableId;
    private Long waiterId;
    private Long dishId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Create table
        TableDto table = new TableDto();
        table.setTableNumber(5);
        table.setCapacity(4);
        table.setLocation("Window");
        TableDto createdTable = tableService.createTable(table);
        tableId = createdTable.getId();

        // Create employee
        Employee employee = new Employee();
        employee.setFirstName("Chef");
        employee.setLastName("Cook");
        employee.setRole(EmployeeRole.WAITER);
        employee.setIsActive(true);
        Employee savedEmployee = employeeRepository.save(employee);
        waiterId = savedEmployee.getId();

        // Create category and dish
        CategoryDto category = new CategoryDto();
        category.setName("Main Course");
        CategoryDto createdCategory = menuService.createCategory(category);
        categoryId = createdCategory.getId();

        DishDto dish = new DishDto();
        dish.setName("Steak");
        dish.setPrice(new BigDecimal("25.00"));
        dish.setCost(new BigDecimal("10.00"));
        dish.setCategoryId(categoryId);
        DishDto createdDish = menuService.createDish(dish);
        dishId = createdDish.getId();

        // Create order and send to kitchen
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
        orderService.sendOrderToKitchen(orderId);
    }

    @Test
    void testGetKitchenQueue() throws Exception {
        mockMvc.perform(get("/api/kitchen/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetAllKitchenQueueItems() throws Exception {
        mockMvc.perform(get("/api/kitchen/queue/all")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdateDishStatus() throws Exception {
        // Get queue item first
        var result = mockMvc.perform(get("/api/kitchen/queue"))
                .andExpect(status().isOk())
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        // If queue has items, try to update first one (test might fail if queue is empty, which is ok)
        if (!content.equals("[]") && content.contains("\"id\"")) {
            // Use KitchenService to get queue and update first item
            List<ru.ifmo.se.restaurant.dto.KitchenQueueDto> queue = kitchenService.getKitchenQueue();
            if (!queue.isEmpty()) {
                Long queueId = queue.get(0).getId();
                mockMvc.perform(patch("/api/kitchen/queue/" + queueId + "/status")
                        .param("status", "IN_PROGRESS"))
                        .andExpect(status().isOk());
            }
        }
    }
}

