package ru.ifmo.se.restaurant.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.client.KitchenServiceClient;
import ru.ifmo.se.restaurant.order.client.MenuServiceClient;
import ru.ifmo.se.restaurant.order.dto.*;
import ru.ifmo.se.restaurant.order.entity.*;
import ru.ifmo.se.restaurant.order.exception.BusinessConflictException;
import ru.ifmo.se.restaurant.order.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.exception.ServiceUnavailableException;
import ru.ifmo.se.restaurant.order.dataaccess.*;
import ru.ifmo.se.restaurant.order.util.PaginationUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderDataAccess orderDataAccess;
    private final OrderItemDataAccess orderItemDataAccess;
    private final TableDataAccess tableDataAccess;
    private final EmployeeDataAccess employeeDataAccess;
    private final KitchenServiceClient kitchenServiceClient;
    private final MenuServiceClient menuServiceClient;

    @Transactional
    public Mono<OrderDto> createOrder(OrderDto dto) {
        return tableDataAccess.getById(dto.getTableId())
            .flatMap(table -> {
                if (table.getStatus() == TableStatus.OCCUPIED) {
                    return Mono.error(new BusinessConflictException(
                        "Cannot create order: table is already occupied",
                        "Table",
                        table.getId(),
                        "Status: OCCUPIED"
                    ));
                }
                return employeeDataAccess.getById(dto.getWaiterId())
                    .flatMap(employee -> {
                        Order order = new Order();
                        order.setTableId(table.getId());
                        order.setWaiterId(employee.getId());
                        order.setStatus(OrderStatus.CREATED);
                        order.setTotalAmount(BigDecimal.ZERO);
                        order.setSpecialRequests(dto.getSpecialRequests());
                        order.setCreatedAt(LocalDateTime.now());

                        return orderDataAccess.save(order)
                            .flatMap(savedOrder -> {
                                table.setStatus(TableStatus.OCCUPIED);
                                return tableDataAccess.save(table)
                                    .thenReturn(savedOrder);
                            })
                            .flatMap(this::toDto);
                    });
            });
    }

    public Mono<OrderDto> getOrderById(Long id) {
        return orderDataAccess.getById(id)
            .flatMap(this::toDto);
    }

    public Flux<OrderDto> getAllOrders() {
        return orderDataAccess.findAll()
            .flatMap(this::toDto);
    }

    @Transactional
    public Mono<OrderDto> addItemToOrder(Long orderId, OrderItemDto itemDto) {
        return orderDataAccess.getById(orderId)
            .flatMap(order -> menuServiceClient.getDish(itemDto.getDishId())
                .flatMap(dish -> {
                    if (dish.getPrice() == null) {
                        return Mono.error(new ServiceUnavailableException(
                            "Menu service returned invalid data for dish " + itemDto.getDishId(),
                            "menu-service",
                            "getDish"
                        ));
                    }
                    OrderItem item = new OrderItem();
                    item.setOrderId(orderId);
                    item.setDishId(dish.getId());
                    item.setDishName(dish.getName());
                    item.setQuantity(itemDto.getQuantity());
                    item.setPrice(dish.getPrice());
                    item.setSpecialRequest(itemDto.getSpecialRequest());

                    return orderItemDataAccess.save(item)
                        .flatMap(savedItem -> {
                            BigDecimal itemTotal = savedItem.getPrice()
                                .multiply(BigDecimal.valueOf(savedItem.getQuantity()));
                            order.setTotalAmount(order.getTotalAmount().add(itemTotal));
                            return orderDataAccess.save(order);
                        });
                }))
            .flatMap(this::toDto);
    }

    @Transactional
    public Mono<Void> removeItemFromOrder(Long orderId, Long itemId) {
        return orderDataAccess.getById(orderId)
            .flatMap(order -> orderItemDataAccess.getById(itemId)
                .flatMap(item -> {
                    BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    order.setTotalAmount(order.getTotalAmount().subtract(itemTotal));
                    return orderDataAccess.save(order)
                        .then(orderItemDataAccess.deleteById(itemId));
                }));
    }

    @Transactional
    public Mono<OrderDto> sendToKitchen(Long orderId) {
        return orderDataAccess.getById(orderId)
            .flatMap(order -> {
                if (order.getStatus() != OrderStatus.CREATED) {
                    return Mono.error(new BusinessConflictException(
                        "Cannot send to kitchen: order must be in CREATED status",
                        "Order",
                        orderId,
                        "Current status: " + order.getStatus()
                    ));
                }
                order.setStatus(OrderStatus.IN_KITCHEN);
                return orderDataAccess.save(order)
                    .flatMap(savedOrder -> orderItemDataAccess.findByOrderId(orderId)
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
        return orderDataAccess.getById(orderId)
            .flatMap(order -> {
                order.setStatus(OrderStatus.CLOSED);
                order.setClosedAt(LocalDateTime.now());
                return orderDataAccess.save(order)
                    .flatMap(savedOrder -> tableDataAccess.findById(order.getTableId())
                        .flatMap(table -> {
                            table.setStatus(TableStatus.FREE);
                            return tableDataAccess.save(table);
                        })
                        .thenReturn(savedOrder))
                    .flatMap(this::toDto);
            });
    }

    private Mono<OrderDto> toDto(Order order) {
        return orderItemDataAccess.findByOrderId(order.getId())
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

    public Mono<Page<OrderDto>> getAllOrdersPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.DESC, "id"));

        return orderDataAccess.count()
            .flatMap(total -> orderDataAccess.findAll(pageable)
                .flatMap(this::toDto)
                .collectList()
                .map(orders -> new PageImpl<>(orders, pageable, total)));
    }

    public Mono<Slice<OrderDto>> getAllOrdersSlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size + 1, Sort.by(Sort.Direction.DESC, "id"));

        return orderDataAccess.findAll(pageable)
            .flatMap(this::toDto)
            .collectList()
            .map(orders -> {
                boolean hasNext = orders.size() > size;
                if (hasNext) {
                    orders = orders.subList(0, size);
                }
                return new SliceImpl<>(orders, PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.DESC, "id")), hasNext);
            });
    }

    public Mono<Page<TableDto>> getAllTablesPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "id"));

        return tableDataAccess.count()
            .flatMap(total -> tableDataAccess.findAll(pageable)
                .map(this::toTableDto)
                .collectList()
                .map(tables -> new PageImpl<>(tables, pageable, total)));
    }

    public Mono<Page<EmployeeDto>> getAllEmployeesPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "id"));

        return employeeDataAccess.count()
            .flatMap(total -> employeeDataAccess.findAll(pageable)
                .map(this::toEmployeeDto)
                .collectList()
                .map(employees -> new PageImpl<>(employees, pageable, total)));
    }

    private TableDto toTableDto(RestaurantTable table) {
        return new TableDto(
            table.getId(),
            table.getTableNumber(),
            table.getCapacity(),
            table.getLocation(),
            table.getStatus()
        );
    }

    private EmployeeDto toEmployeeDto(Employee employee) {
        return new EmployeeDto(
            employee.getId(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getEmail(),
            employee.getPhone(),
            employee.getRole()
        );
    }
}
