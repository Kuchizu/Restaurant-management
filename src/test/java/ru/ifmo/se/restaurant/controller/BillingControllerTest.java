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
import ru.ifmo.se.restaurant.model.EmployeeRole;
import ru.ifmo.se.restaurant.model.OrderStatus;
import ru.ifmo.se.restaurant.model.entity.Employee;
import ru.ifmo.se.restaurant.model.entity.Order;
import ru.ifmo.se.restaurant.repository.EmployeeRepository;
import ru.ifmo.se.restaurant.repository.OrderRepository;
import ru.ifmo.se.restaurant.service.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
class BillingControllerTest extends BaseIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BillingService billingService;

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
        table.setTableNumber(3);
        table.setCapacity(4);
        table.setLocation("Center");
        TableDto createdTable = tableService.createTable(table);
        tableId = createdTable.getId();

        // Create employee
        Employee employee = new Employee();
        employee.setFirstName("Jane");
        employee.setLastName("Smith");
        employee.setRole(EmployeeRole.WAITER);
        employee.setIsActive(true);
        Employee savedEmployee = employeeRepository.save(employee);
        waiterId = savedEmployee.getId();

        // Create category and dish
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

        // Set order status to READY
        Order orderEntity = orderRepository.findById(orderId).orElseThrow();
        orderEntity.setStatus(OrderStatus.READY);
        orderRepository.save(orderEntity);
    }

    @Test
    void testFinalizeOrder() throws Exception {
        mockMvc.perform(post("/api/billing/orders/" + orderId + "/finalize")
                .param("discount", "0")
                .param("notes", "Test bill"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.total").exists());
    }

    @Test
    void testFinalizeOrderWithDiscount() throws Exception {
        mockMvc.perform(post("/api/billing/orders/" + orderId + "/finalize")
                .param("discount", "1.00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.discount").value(1.0));
    }

    @Test
    void testGetBillByOrderId() throws Exception {
        // First finalize order
        billingService.finalizeOrder(orderId, BigDecimal.ZERO, "Test");

        mockMvc.perform(get("/api/billing/orders/" + orderId + "/bill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId));
    }
}

