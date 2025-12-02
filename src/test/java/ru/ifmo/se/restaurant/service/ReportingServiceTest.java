package ru.ifmo.se.restaurant.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.DishDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportingServiceTest extends BaseIntegrationTest {
    @Autowired
    private ReportingService reportingService;

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

        Map<String, Object> result = reportingService.getPopularDishes(startDate, endDate, 10);
        assertNotNull(result);
        assertTrue(result.containsKey("popularDishes"));
        assertTrue(result.containsKey("startDate"));
        assertTrue(result.containsKey("endDate"));
    }

    @Test
    void testGetProfitability() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        Map<String, Object> result = reportingService.getProfitability(startDate, endDate);
        assertNotNull(result);
        assertTrue(result.containsKey("revenue"));
        assertTrue(result.containsKey("totalCost"));
        assertTrue(result.containsKey("profit"));
        assertTrue(result.containsKey("profitMargin"));
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

        Map<String, Object> result = reportingService.getPopularDishes(startDate, endDate, 5);
        assertNotNull(result);
        assertTrue(result.containsKey("popularDishes"));
        assertEquals(startDate, result.get("startDate"));
        assertEquals(endDate, result.get("endDate"));
    }

    @Test
    void testGetProfitabilityWithZeroRevenue() {
        LocalDateTime startDate = LocalDateTime.now().minusYears(10);
        LocalDateTime endDate = LocalDateTime.now().minusYears(9);

        Map<String, Object> result = reportingService.getProfitability(startDate, endDate);
        assertNotNull(result);
        assertTrue(result.containsKey("revenue"));
        assertTrue(result.containsKey("totalCost"));
        assertTrue(result.containsKey("profit"));
        assertTrue(result.containsKey("profitMargin"));
        assertEquals(BigDecimal.ZERO, result.get("profitMargin"));
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

        Map<String, Object> result = reportingService.getPopularDishes(startDate, endDate, 10);
        assertNotNull(result);
        assertTrue(result.containsKey("popularDishes"));
    }
}

