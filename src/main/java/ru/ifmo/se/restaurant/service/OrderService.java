package ru.ifmo.se.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ifmo.se.restaurant.dataaccess.OrderDataAccess;
import ru.ifmo.se.restaurant.dto.OrderDto;
import ru.ifmo.se.restaurant.dto.OrderItemDto;
import ru.ifmo.se.restaurant.exception.BusinessException;
import ru.ifmo.se.restaurant.model.entity.*;
import ru.ifmo.se.restaurant.model.OrderStatus;
import ru.ifmo.se.restaurant.model.TableStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderDataAccess orderDataAccess;
    private final KitchenService kitchenService;

    public OrderService(OrderDataAccess orderDataAccess,
                       KitchenService kitchenService) {
        this.orderDataAccess = orderDataAccess;
        this.kitchenService = kitchenService;
    }

    @Transactional
    public OrderDto createOrder(OrderDto dto) {
        Table table = orderDataAccess.findTableById(dto.getTableId());

        if (table.getStatus() == TableStatus.OCCUPIED) {
            throw new BusinessException("Table is already occupied");
        }

        Employee waiter = orderDataAccess.findWaiterById(dto.getWaiterId());

        Order order = new Order();
        order.setTable(table);
        order.setWaiter(waiter);
        order.setStatus(OrderStatus.CREATED);
        order.setSpecialRequests(dto.getSpecialRequests());
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.ZERO);
        order.setItems(new ArrayList<>());

        Order savedOrder = orderDataAccess.saveOrder(order);

        BigDecimal total = BigDecimal.ZERO;
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (OrderItemDto itemDto : dto.getItems()) {
                Dish dish = orderDataAccess.findActiveDishById(itemDto.getDishId());

                OrderItem item = new OrderItem();
                item.setOrder(savedOrder);
                item.setDish(dish);
                item.setQuantity(itemDto.getQuantity());
                item.setPrice(dish.getPrice());
                item.setSpecialRequest(itemDto.getSpecialRequest());

                OrderItem savedItem = orderDataAccess.saveOrderItem(item);
                savedOrder.getItems().add(savedItem);
                
                total = total.add(dish.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            }
            savedOrder.setTotalAmount(total);
            savedOrder = orderDataAccess.saveOrder(savedOrder);
        }

        table.setStatus(TableStatus.OCCUPIED);
        orderDataAccess.saveTable(table);

        return toOrderDto(savedOrder);
    }

    @Transactional
    public OrderDto addItemToOrder(Long orderId, OrderItemDto itemDto) {
        Order order = orderDataAccess.findOrderById(orderId);
        
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.IN_KITCHEN) {
            throw new BusinessException("Cannot add items to order with status: " + order.getStatus());
        }

        Dish dish = orderDataAccess.findActiveDishById(itemDto.getDishId());

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setDish(dish);
        item.setQuantity(itemDto.getQuantity());
        item.setPrice(dish.getPrice());
        item.setSpecialRequest(itemDto.getSpecialRequest());

        OrderItem savedItem = orderDataAccess.saveOrderItem(item);
        order.getItems().add(savedItem);

        BigDecimal itemTotal = dish.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
        order.setTotalAmount(order.getTotalAmount().add(itemTotal));
        
        order = orderDataAccess.saveOrder(order);
        return toOrderDto(order);
    }

    @Transactional
    public void removeItemFromOrder(Long orderId, Long itemId) {
        Order order = orderDataAccess.findOrderById(orderId);
        
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.IN_KITCHEN) {
            throw new BusinessException("Cannot remove items from order with status: " + order.getStatus());
        }

        OrderItem item = orderDataAccess.findOrderItemById(itemId);

        if (!item.getOrder().getId().equals(orderId)) {
            throw new BusinessException("Order item does not belong to this order");
        }

        BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        order.setTotalAmount(order.getTotalAmount().subtract(itemTotal));
        
        orderDataAccess.deleteOrderItem(item);
        order.getItems().remove(item);
        orderDataAccess.saveOrder(order);
    }

    @Transactional
    public OrderDto sendOrderToKitchen(Long orderId) {
        Order order = orderDataAccess.findOrderById(orderId);
        
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Order can only be sent to kitchen from CREATED status");
        }

        if (order.getItems().isEmpty()) {
            throw new BusinessException("Cannot send empty order to kitchen");
        }

        order.setStatus(OrderStatus.IN_KITCHEN);
        order = orderDataAccess.saveOrder(order);

        kitchenService.addOrderToKitchenQueue(order);

        return toOrderDto(order);
    }

    @Transactional
    public OrderDto closeOrder(Long orderId) {
        Order order = orderDataAccess.findOrderById(orderId);
        
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("Order can only be closed from DELIVERED status");
        }

        order.setStatus(OrderStatus.CLOSED);
        order.setClosedAt(LocalDateTime.now());
        order = orderDataAccess.saveOrder(order);

        Table table = order.getTable();
        table.setStatus(TableStatus.FREE);
        orderDataAccess.saveTable(table);

        return toOrderDto(order);
    }

    public OrderDto getOrderById(Long id) {
        Order order = orderDataAccess.findOrderById(id);
        return toOrderDto(order);
    }

    public Page<OrderDto> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return orderDataAccess.findAllOrders(pageable).map(this::toOrderDto);
    }

    private OrderDto toOrderDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setTableId(order.getTable().getId());
        dto.setTableNumber(order.getTable().getTableNumber());
        dto.setWaiterId(order.getWaiter().getId());
        dto.setWaiterName(order.getWaiter().getFirstName() + " " + order.getWaiter().getLastName());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setSpecialRequests(order.getSpecialRequests());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setClosedAt(order.getClosedAt());
        dto.setItems(order.getItems().stream().map(this::toOrderItemDto).collect(Collectors.toList()));
        return dto;
    }

    private OrderItemDto toOrderItemDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setDishId(item.getDish().getId());
        dto.setDishName(item.getDish().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSpecialRequest(item.getSpecialRequest());
        return dto;
    }
}

