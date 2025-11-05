package ru.ifmo.se.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.*;
import ru.ifmo.se.restaurant.model.EmployeeRole;
import ru.ifmo.se.restaurant.model.entity.Employee;
import ru.ifmo.se.restaurant.repository.EmployeeRepository;
import ru.ifmo.se.restaurant.repository.OrderRepository;
import ru.ifmo.se.restaurant.service.MenuManagementService;
import ru.ifmo.se.restaurant.service.OrderService;
import ru.ifmo.se.restaurant.service.TableManagementService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
class OrderControllerTest extends BaseIntegrationTest {
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
    private OrderRepository orderRepository;

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
        table.setTableNumber(1);
        table.setCapacity(4);
        table.setLocation("Window");
        TableDto createdTable = tableService.createTable(table);
        tableId = createdTable.getId();

        // Create employee
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
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
        dish.setName("Pasta");
        dish.setPrice(new BigDecimal("15.00"));
        dish.setCost(new BigDecimal("5.00"));
        dish.setCategoryId(categoryId);
        DishDto createdDish = menuService.createDish(dish);
        dishId = createdDish.getId();

        // Create order
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
    }

    @Test
    void testCreateOrder() throws Exception {
        // Create a new free table for this test
        TableDto newTable = new TableDto();
        newTable.setTableNumber((int)(System.currentTimeMillis() % 10000)); // Unique table number
        newTable.setCapacity(4);
        newTable.setLocation("Test");
        TableDto createdTable = tableService.createTable(newTable);
        
        OrderDto order = new OrderDto();
        order.setTableId(createdTable.getId());
        order.setWaiterId(waiterId);
        OrderItemDto item = new OrderItemDto();
        item.setDishId(dishId);
        item.setQuantity(1);
        List<OrderItemDto> items = new ArrayList<>();
        items.add(item);
        order.setItems(items);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tableId").value(createdTable.getId()));
    }

    @Test
    void testGetOrder() throws Exception {
        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    void testGetAllOrders() throws Exception {
        mockMvc.perform(get("/api/orders")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // @Test
    // void testAddItemToOrder() throws Exception {
    //     // Use the order from setUp (it should be in CREATED status)
    //     // Just verify we can add an item to it
    //     OrderItemDto item = new OrderItemDto();
    //     item.setDishId(dishId);
    //     item.setQuantity(1);

    //     // The orderId from setUp should be in CREATED status and ready for adding items
    //     // Note: If order was already sent to kitchen by another test, status will be IN_KITCHEN
    //     // which still allows adding items
    //     mockMvc.perform(post("/api/orders/" + orderId + "/items")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(item)))
    //             .andExpect(status().isOk());
    // }

    @Test
    void testSendOrderToKitchen() throws Exception {
        mockMvc.perform(post("/api/orders/" + orderId + "/send-to-kitchen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    void testCloseOrder() throws Exception {
        // Create a fresh order and set it to DELIVERED status
        TableDto newTable = new TableDto();
        newTable.setTableNumber((int)((System.currentTimeMillis() + 1) % 10000)); // Unique table number
        newTable.setCapacity(4);
        newTable.setLocation("Close Test");
        TableDto createdTable = tableService.createTable(newTable);
        
        OrderDto order = new OrderDto();
        order.setTableId(createdTable.getId());
        order.setWaiterId(waiterId);
        OrderItemDto item = new OrderItemDto();
        item.setDishId(dishId);
        item.setQuantity(1);
        List<OrderItemDto> items = new ArrayList<>();
        items.add(item);
        order.setItems(items);
        OrderDto created = orderService.createOrder(order);
        Long testOrderId = created.getId();
        
        // Set order status to DELIVERED manually (required for closing)
        ru.ifmo.se.restaurant.model.entity.Order orderEntity = orderRepository.findById(testOrderId).orElseThrow();
        orderEntity.setStatus(ru.ifmo.se.restaurant.model.OrderStatus.DELIVERED);
        orderRepository.save(orderEntity);
        
        mockMvc.perform(post("/api/orders/" + testOrderId + "/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }
}

