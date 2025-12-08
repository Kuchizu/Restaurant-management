package ru.ifmo.se.restaurant.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.client.KitchenServiceClient;
import ru.ifmo.se.restaurant.order.client.MenuServiceClient;
import ru.ifmo.se.restaurant.order.dto.*;
import ru.ifmo.se.restaurant.order.entity.*;
import ru.ifmo.se.restaurant.order.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableRepository tableRepository;
    private final EmployeeRepository employeeRepository;
    private final KitchenServiceClient kitchenServiceClient;
    private final MenuServiceClient menuServiceClient;

    @Transactional
    public Mono<OrderDto> createOrder(OrderDto dto) {
        return tableRepository.findById(dto.getTableId())
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Table not found")))
            .flatMap(table -> {
                if (table.getStatus() == TableStatus.OCCUPIED) {
                    return Mono.error(new RuntimeException("Table is already occupied"));
                }
                return employeeRepository.findById(dto.getWaiterId())
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Employee not found")))
                    .flatMap(employee -> {
                        Order order = new Order();
                        order.setTableId(table.getId());
                        order.setWaiterId(employee.getId());
                        order.setStatus(OrderStatus.CREATED);
                        order.setTotalAmount(BigDecimal.ZERO);
                        order.setSpecialRequests(dto.getSpecialRequests());
                        order.setCreatedAt(LocalDateTime.now());

                        return orderRepository.save(order)
                            .flatMap(savedOrder -> {
                                table.setStatus(TableStatus.OCCUPIED);
                                return tableRepository.save(table)
                                    .thenReturn(savedOrder);
                            })
                            .flatMap(this::toDto);
                    });
            });
    }

    public Mono<OrderDto> getOrderById(Long id) {
        return orderRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order not found")))
            .flatMap(this::toDto);
    }

    public Flux<OrderDto> getAllOrders() {
        return orderRepository.findAll()
            .flatMap(this::toDto);
    }

    @Transactional
    public Mono<OrderDto> addItemToOrder(Long orderId, OrderItemDto itemDto) {
        return orderRepository.findById(orderId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order not found")))
            .flatMap(order -> menuServiceClient.getDish(itemDto.getDishId())
                .flatMap(dish -> {
                    OrderItem item = new OrderItem();
                    item.setOrderId(orderId);
                    item.setDishId(dish.getId());
                    item.setDishName(dish.getName());
                    item.setQuantity(itemDto.getQuantity());
                    item.setPrice(dish.getPrice() != null ? dish.getPrice() : BigDecimal.ZERO);
                    item.setSpecialRequest(itemDto.getSpecialRequest());

                    return orderItemRepository.save(item)
                        .flatMap(savedItem -> {
                            BigDecimal itemTotal = savedItem.getPrice()
                                .multiply(BigDecimal.valueOf(savedItem.getQuantity()));
                            order.setTotalAmount(order.getTotalAmount().add(itemTotal));
                            return orderRepository.save(order);
                        });
                }))
            .flatMap(this::toDto);
    }

    @Transactional
    public Mono<Void> removeItemFromOrder(Long orderId, Long itemId) {
        return orderRepository.findById(orderId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order not found")))
            .flatMap(order -> orderItemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order item not found")))
                .flatMap(item -> {
                    BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    order.setTotalAmount(order.getTotalAmount().subtract(itemTotal));
                    return orderRepository.save(order)
                        .then(orderItemRepository.deleteById(itemId));
                }));
    }

    @Transactional
    public Mono<OrderDto> sendToKitchen(Long orderId) {
        return orderRepository.findById(orderId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order not found")))
            .flatMap(order -> {
                if (order.getStatus() != OrderStatus.CREATED) {
                    return Mono.error(new RuntimeException("Order must be in CREATED status"));
                }
                order.setStatus(OrderStatus.IN_KITCHEN);
                return orderRepository.save(order)
                    .flatMap(savedOrder -> orderItemRepository.findByOrderId(orderId)
                        .flatMap(item -> {
                            KitchenQueueRequest request = new KitchenQueueRequest(
                                orderId, item.getId(), item.getDishName(),
                                item.getQuantity(), item.getSpecialRequest()
                            );
                            return kitchenServiceClient.addToKitchenQueue(request);
                        })
                        .then(Mono.just(savedOrder)))
                    .flatMap(this::toDto);
            });
    }

    @Transactional
    public Mono<OrderDto> closeOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order not found")))
            .flatMap(order -> {
                order.setStatus(OrderStatus.CLOSED);
                order.setClosedAt(LocalDateTime.now());
                return orderRepository.save(order)
                    .flatMap(savedOrder -> tableRepository.findById(order.getTableId())
                        .flatMap(table -> {
                            table.setStatus(TableStatus.FREE);
                            return tableRepository.save(table);
                        })
                        .thenReturn(savedOrder))
                    .flatMap(this::toDto);
            });
    }

    private Mono<OrderDto> toDto(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
            .map(item -> new OrderItemDto(
                item.getId(), item.getDishId(), item.getDishName(),
                item.getQuantity(), item.getPrice(), item.getSpecialRequest()
            ))
            .collectList()
            .map(items -> {
                OrderDto dto = new OrderDto();
                dto.setId(order.getId());
                dto.setTableId(order.getTableId());
                dto.setWaiterId(order.getWaiterId());
                dto.setStatus(order.getStatus());
                dto.setTotalAmount(order.getTotalAmount());
                dto.setSpecialRequests(order.getSpecialRequests());
                dto.setCreatedAt(order.getCreatedAt());
                dto.setClosedAt(order.getClosedAt());
                dto.setItems(items);
                return dto;
            });
    }
}
