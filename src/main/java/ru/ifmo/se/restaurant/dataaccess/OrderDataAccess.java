package ru.ifmo.se.restaurant.dataaccess;

import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.model.entity.*;
import ru.ifmo.se.restaurant.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Component
public class OrderDataAccess {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableRepository tableRepository;
    private final EmployeeRepository employeeRepository;
    private final DishRepository dishRepository;

    public OrderDataAccess(OrderRepository orderRepository,
                          OrderItemRepository orderItemRepository,
                          TableRepository tableRepository,
                          EmployeeRepository employeeRepository,
                          DishRepository dishRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.tableRepository = tableRepository;
        this.employeeRepository = employeeRepository;
        this.dishRepository = dishRepository;
    }

    public Table findTableById(Long id) {
        return tableRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));
    }

    public Employee findWaiterById(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    public Dish findActiveDishById(Long id) {
        return dishRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + id));
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public OrderItem saveOrderItem(OrderItem item) {
        return orderItemRepository.save(item);
    }

    public Order findOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public OrderItem findOrderItemById(Long id) {
        return orderItemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + id));
    }

    public Table saveTable(Table table) {
        return tableRepository.save(table);
    }

    public void deleteOrderItem(OrderItem item) {
        orderItemRepository.delete(item);
    }

    public Page<Order> findAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
}
