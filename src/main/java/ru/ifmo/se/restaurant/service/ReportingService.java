package ru.ifmo.se.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.dto.DishDto;
import ru.ifmo.se.restaurant.dto.DishPopularityDto;
import ru.ifmo.se.restaurant.dto.PopularDishesReportDto;
import ru.ifmo.se.restaurant.dto.ProfitabilityReportDto;
import ru.ifmo.se.restaurant.repository.BillRepository;
import ru.ifmo.se.restaurant.repository.DishRepository;
import ru.ifmo.se.restaurant.repository.OrderItemRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportingService {
    private final BillRepository billRepository;
    private final OrderItemRepository orderItemRepository;
    private final DishRepository dishRepository;

    public ReportingService(BillRepository billRepository,
                          OrderItemRepository orderItemRepository,
                          DishRepository dishRepository) {
        this.billRepository = billRepository;
        this.orderItemRepository = orderItemRepository;
        this.dishRepository = dishRepository;
    }

    public BigDecimal getRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = billRepository.getTotalRevenue(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public PopularDishesReportDto getPopularDishes(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        List<Object[]> results = orderItemRepository.findPopularDishes(startDate, endDate);

        List<DishPopularityDto> popularDishes = results.stream()
            .limit(limit)
            .map(row -> new DishPopularityDto(
                ((Number) row[0]).longValue(),
                (String) row[1],
                ((Number) row[2]).intValue()
            ))
            .collect(Collectors.toList());

        return new PopularDishesReportDto(startDate, endDate, popularDishes);
    }

    public ProfitabilityReportDto getProfitability(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = getRevenue(startDate, endDate);
        BigDecimal totalCost = orderItemRepository.calculateTotalCost(startDate, endDate);

        if (totalCost == null) {
            totalCost = BigDecimal.ZERO;
        }

        BigDecimal profit = revenue.subtract(totalCost);
        BigDecimal profitMargin = revenue.compareTo(BigDecimal.ZERO) > 0
            ? profit.divide(revenue, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
            : BigDecimal.ZERO;

        return new ProfitabilityReportDto(startDate, endDate, revenue, totalCost, profit, profitMargin);
    }

    public Page<DishDto> getDishesByRevenue(int page, int size, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        List<Object[]> results = orderItemRepository.findDishesByRevenue(startDate, endDate);

        int start = page * size;
        int end = Math.min(start + size, results.size());

        if (start >= results.size()) {
            return dishRepository.findAll(pageable).map(this::convertToDishDto);
        }

        List<Long> dishIds = results.subList(start, end).stream()
            .map(row -> ((Number) row[0]).longValue())
            .collect(Collectors.toList());

        return dishRepository.findAll(pageable).map(this::convertToDishDto);
    }

    private DishDto convertToDishDto(ru.ifmo.se.restaurant.model.entity.Dish dish) {
        DishDto dto = new DishDto();
        dto.setId(dish.getId());
        dto.setName(dish.getName());
        dto.setPrice(dish.getPrice());
        dto.setCost(dish.getCost());
        return dto;
    }
}
