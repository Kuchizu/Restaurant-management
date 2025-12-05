package ru.ifmo.se.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ru.ifmo.se.restaurant.dataaccess.ReportingDataAccess;
import ru.ifmo.se.restaurant.dto.DishDto;
import ru.ifmo.se.restaurant.dto.DishPopularityDto;
import ru.ifmo.se.restaurant.dto.PopularDishesReportDto;
import ru.ifmo.se.restaurant.dto.ProfitabilityReportDto;
import ru.ifmo.se.restaurant.model.entity.Dish;
import ru.ifmo.se.restaurant.model.entity.Order;
import ru.ifmo.se.restaurant.model.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportingService {
    private final ReportingDataAccess reportingDataAccess;

    public ReportingService(ReportingDataAccess reportingDataAccess) {
        this.reportingDataAccess = reportingDataAccess;
    }

    public BigDecimal getRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = reportingDataAccess.getTotalRevenue(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public PopularDishesReportDto getPopularDishes(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        List<Order> orders = reportingDataAccess.findOrdersByDateRange(startDate, endDate);
        Map<Long, Integer> dishQuantityMap = new HashMap<>();
        Map<Long, Dish> dishMap = new HashMap<>();

        for (Order order : orders) {
            List<OrderItem> items = reportingDataAccess.findOrderItemsByOrderId(order.getId());
            for (OrderItem item : items) {
                Long dishId = item.getDish().getId();
                dishQuantityMap.merge(dishId, item.getQuantity(), Integer::sum);
                dishMap.putIfAbsent(dishId, item.getDish());
            }
        }

        List<DishPopularityDto> popularDishes = dishQuantityMap.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(limit)
            .map(entry -> {
                Dish dish = dishMap.get(entry.getKey());
                return new DishPopularityDto(dish.getId(), dish.getName(), entry.getValue());
            })
            .collect(Collectors.toList());

        return new PopularDishesReportDto(startDate, endDate, popularDishes);
    }

    public ProfitabilityReportDto getProfitability(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = getRevenue(startDate, endDate);

        List<Order> orders = reportingDataAccess.findOrdersByDateRange(startDate, endDate);
        BigDecimal totalCost = BigDecimal.ZERO;

        for (Order order : orders) {
            List<OrderItem> items = reportingDataAccess.findOrderItemsByOrderId(order.getId());
            for (OrderItem item : items) {
                BigDecimal itemCost = item.getDish().getCost().multiply(BigDecimal.valueOf(item.getQuantity()));
                totalCost = totalCost.add(itemCost);
            }
        }

        BigDecimal profit = revenue.subtract(totalCost);
        BigDecimal profitMargin = revenue.compareTo(BigDecimal.ZERO) > 0
            ? profit.divide(revenue, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
            : BigDecimal.ZERO;

        return new ProfitabilityReportDto(startDate, endDate, revenue, totalCost, profit, profitMargin);
    }

    public Page<DishDto> getDishesByRevenue(int page, int size, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = reportingDataAccess.findOrdersByDateRange(startDate, endDate);
        
        Map<Long, BigDecimal> dishRevenueMap = new HashMap<>();
        Map<Long, Dish> dishMap = new HashMap<>();

        for (Order order : orders) {
            List<OrderItem> items = reportingDataAccess.findOrderItemsByOrderId(order.getId());
            for (OrderItem item : items) {
                Long dishId = item.getDish().getId();
                BigDecimal itemRevenue = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                dishRevenueMap.merge(dishId, itemRevenue, BigDecimal::add);
                dishMap.putIfAbsent(dishId, item.getDish());
            }
        }

        List<Dish> sortedDishes = dishRevenueMap.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .map(entry -> dishMap.get(entry.getKey()))
            .collect(Collectors.toList());

        int totalElements = sortedDishes.size();
        int start = page * size;
        int end = Math.min(start + size, sortedDishes.size());
        List<Dish> pagedDishes = start < sortedDishes.size() 
            ? sortedDishes.subList(start, end) 
            : List.of();
        
        List<DishDto> dishDtos = pagedDishes.stream()
            .map(dish -> {
                DishDto dto = new DishDto();
                dto.setId(dish.getId());
                dto.setName(dish.getName());
                dto.setPrice(dish.getPrice());
                dto.setCost(dish.getCost());
                return dto;
            })
            .collect(Collectors.toList());
        
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(dishDtos, pageable, totalElements);
    }
}

