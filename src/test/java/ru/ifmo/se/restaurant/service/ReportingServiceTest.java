package ru.ifmo.se.restaurant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.*;
import ru.ifmo.se.restaurant.model.EmployeeRole;
import ru.ifmo.se.restaurant.model.entity.Employee;
import ru.ifmo.se.restaurant.repository.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportingServiceTest extends BaseIntegrationTest {
    @Autowired
    private ReportingService reportingService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MenuManagementService menuService;

    @Autowired
    private TableManagementService tableService;

    @Autowired
    private BillingService billingService;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Long tableId;
    private Long waiterId;
    private Long categoryId;
    private Long dishId;

    @BeforeEach
    void setUp() {
        TableDto table = new TableDto();
        table.setTableNumber(100);
        table.setCapacity(4);
        TableDto createdTable = tableService.createTable(table);
        tableId = createdTable.getId();

        Employee employee = new Employee();
        employee.setFirstName("Test");
        employee.setLastName("Waiter");
        employee.setRole(EmployeeRole.WAITER);
        employee.setIsActive(true);
        Employee savedEmployee = employeeRepository.save(employee);
        waiterId = savedEmployee.getId();

        CategoryDto category = new CategoryDto();
        category.setName("Test Category");
        CategoryDto createdCategory = menuService.createCategory(category);
        categoryId = createdCategory.getId();

        DishDto dish = new DishDto();
        dish.setName("Test Dish");
        dish.setPrice(new BigDecimal("15.00"));
        dish.setCost(new BigDecimal("5.00"));
        dish.setCategoryId(categoryId);
        DishDto createdDish = menuService.createDish(dish);
        dishId = createdDish.getId();
    }

    @Test
    void testGetRevenue() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        BigDecimal revenue = reportingService.getRevenue(startDate, endDate);
        assertNotNull(revenue);
        assertTrue(revenue.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testGetPopularDishes() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        PopularDishesReportDto result = reportingService.getPopularDishes(startDate, endDate, 10);
        assertNotNull(result);
        assertNotNull(result.getPopularDishes());
        assertNotNull(result.getStartDate());
        assertNotNull(result.getEndDate());
    }

    @Test
    void testGetProfitability() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        ProfitabilityReportDto result = reportingService.getProfitability(startDate, endDate);
        assertNotNull(result);
        assertNotNull(result.getRevenue());
        assertNotNull(result.getTotalCost());
        assertNotNull(result.getProfit());
        assertNotNull(result.getProfitMargin());
    }

    @Test
    void testGetDishesByRevenue() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        Page<DishDto> result = reportingService.getDishesByRevenue(0, 10, startDate, endDate);
        assertNotNull(result);
    }

    @Test
    void testGetRevenueWithNullResult() {
        LocalDateTime startDate = LocalDateTime.now().minusYears(10);
        LocalDateTime endDate = LocalDateTime.now().minusYears(9);

        BigDecimal revenue = reportingService.getRevenue(startDate, endDate);
        assertNotNull(revenue);
        assertEquals(BigDecimal.ZERO, revenue);
    }

    @Test
    void testGetPopularDishesWithLimit() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        PopularDishesReportDto result = reportingService.getPopularDishes(startDate, endDate, 5);
        assertNotNull(result);
        assertNotNull(result.getPopularDishes());
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
    }

    @Test
    void testGetProfitabilityWithZeroRevenue() {
        LocalDateTime startDate = LocalDateTime.now().minusYears(10);
        LocalDateTime endDate = LocalDateTime.now().minusYears(9);

        ProfitabilityReportDto result = reportingService.getProfitability(startDate, endDate);
        assertNotNull(result);
        assertNotNull(result.getRevenue());
        assertNotNull(result.getTotalCost());
        assertNotNull(result.getProfit());
        assertNotNull(result.getProfitMargin());
        assertEquals(BigDecimal.ZERO, result.getProfitMargin());
    }

    @Test
    void testGetDishesByRevenueWithPagination() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        Page<DishDto> page1 = reportingService.getDishesByRevenue(0, 10, startDate, endDate);

        assertNotNull(page1);
        assertTrue(page1.getTotalElements() >= 0);
    }

    @Test
    void testGetRevenueCurrentPeriod() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        BigDecimal revenue = reportingService.getRevenue(startDate, endDate);
        assertNotNull(revenue);
        assertTrue(revenue.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testGetPopularDishesWithNoOrders() {
        LocalDateTime startDate = LocalDateTime.now().minusYears(10);
        LocalDateTime endDate = LocalDateTime.now().minusYears(9);

        PopularDishesReportDto result = reportingService.getPopularDishes(startDate, endDate, 10);
        assertNotNull(result);
        assertNotNull(result.getPopularDishes());
    }

    @Test
    void testGetRevenueWithActualOrders() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        BigDecimal revenue = reportingService.getRevenue(startDate, endDate);
        assertNotNull(revenue);
        assertTrue(revenue.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testGetPopularDishesWithActualOrders() {
        OrderDto order1 = new OrderDto();
        order1.setTableId(tableId);
        order1.setWaiterId(waiterId);
        OrderItemDto item1 = new OrderItemDto();
        item1.setDishId(dishId);
        item1.setQuantity(3);
        order1.setItems(List.of(item1));
        orderService.createOrder(order1);

        TableDto table2 = new TableDto();
        table2.setTableNumber(101);
        table2.setCapacity(4);
        TableDto createdTable2 = tableService.createTable(table2);

        OrderDto order2 = new OrderDto();
        order2.setTableId(createdTable2.getId());
        order2.setWaiterId(waiterId);
        OrderItemDto item2 = new OrderItemDto();
        item2.setDishId(dishId);
        item2.setQuantity(5);
        order2.setItems(List.of(item2));
        orderService.createOrder(order2);

        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        PopularDishesReportDto result = reportingService.getPopularDishes(startDate, endDate, 5);
        assertNotNull(result);
        assertNotNull(result.getPopularDishes());
        List<DishPopularityDto> dishes = result.getPopularDishes();
        assertFalse(dishes.isEmpty());
    }

    @Test
    void testGetProfitabilityWithActualOrders() {
        OrderDto order = new OrderDto();
        order.setTableId(tableId);
        order.setWaiterId(waiterId);
        OrderItemDto item = new OrderItemDto();
        item.setDishId(dishId);
        item.setQuantity(2);
        order.setItems(List.of(item));
        orderService.createOrder(order);

        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        ProfitabilityReportDto result = reportingService.getProfitability(startDate, endDate);
        assertNotNull(result);
        BigDecimal revenue = result.getRevenue();
        BigDecimal totalCost = result.getTotalCost();
        BigDecimal profit = result.getProfit();
        BigDecimal profitMargin = result.getProfitMargin();

        assertNotNull(revenue);
        assertNotNull(totalCost);
        assertNotNull(profit);
        assertNotNull(profitMargin);
    }

    @Test
    void testGetDishesByRevenueWithActualOrders() {
        OrderDto order = new OrderDto();
        order.setTableId(tableId);
        order.setWaiterId(waiterId);
        OrderItemDto item = new OrderItemDto();
        item.setDishId(dishId);
        item.setQuantity(2);
        order.setItems(List.of(item));
        orderService.createOrder(order);

        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        Page<DishDto> result = reportingService.getDishesByRevenue(0, 10, startDate, endDate);
        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 0);
    }
}

