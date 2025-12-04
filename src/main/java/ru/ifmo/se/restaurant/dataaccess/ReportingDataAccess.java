package ru.ifmo.se.restaurant.dataaccess;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.model.entity.Dish;
import ru.ifmo.se.restaurant.model.entity.Order;
import ru.ifmo.se.restaurant.model.entity.OrderItem;
import ru.ifmo.se.restaurant.repository.BillRepository;
import ru.ifmo.se.restaurant.repository.DishRepository;
import ru.ifmo.se.restaurant.repository.OrderItemRepository;
import ru.ifmo.se.restaurant.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReportingDataAccess {
    private final BillRepository billRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DishRepository dishRepository;

    public ReportingDataAccess(BillRepository billRepository,
                              OrderRepository orderRepository,
                              OrderItemRepository orderItemRepository,
                              DishRepository dishRepository) {
        this.billRepository = billRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.dishRepository = dishRepository;
    }

    public BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return billRepository.getTotalRevenue(startDate, endDate);
    }

    public List<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByDateRange(startDate, endDate);
    }

    public List<OrderItem> findOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public Page<Dish> findAllDishesPage(Pageable pageable) {
        return dishRepository.findAll(pageable);
    }
}
