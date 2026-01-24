package ru.ifmo.se.restaurant.order.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.ifmo.se.restaurant.order.application.dto.*;
import ru.ifmo.se.restaurant.order.application.port.in.*;
import ru.ifmo.se.restaurant.order.application.port.out.*;
import ru.ifmo.se.restaurant.order.domain.entity.*;
import ru.ifmo.se.restaurant.order.domain.exception.BusinessConflictException;
import ru.ifmo.se.restaurant.order.domain.exception.ServiceUnavailableException;
import ru.ifmo.se.restaurant.order.domain.valueobject.OrderStatus;
import ru.ifmo.se.restaurant.order.domain.valueobject.TableStatus;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.in.web.client.MenuServiceClient;
import ru.ifmo.se.restaurant.order.infrastructure.util.PaginationUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements
        CreateOrderUseCase,
        GetOrderUseCase,
        ManageOrderItemsUseCase,
        OrderWorkflowUseCase,
        ManageTableUseCase,
        ManageEmployeeUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderItemRepositoryPort orderItemRepositoryPort;
    private final TableRepositoryPort tableRepositoryPort;
    private final EmployeeRepositoryPort employeeRepositoryPort;
    private final MenuServiceClient menuServiceClient;
    private final OrderEventPublisher orderEventPublisher;

    @Override
    @Transactional
    public Mono<OrderDto> createOrder(OrderDto dto) {
        return tableRepositoryPort.getById(dto.getTableId())
            .flatMap(table -> {
                if (table.getStatus() == TableStatus.OCCUPIED) {
                    return Mono.error(new BusinessConflictException(
                        "Cannot create order: table is already occupied",
                        "Table",
                        table.getId(),
                        "Status: OCCUPIED"
                    ));
                }
                return employeeRepositoryPort.getById(dto.getWaiterId())
                    .flatMap(employee -> {
                        Order order = new Order();
                        order.setTableId(table.getId());
                        order.setWaiterId(employee.getId());
                        order.setStatus(OrderStatus.CREATED);
                        order.setTotalAmount(BigDecimal.ZERO);
                        order.setSpecialRequests(dto.getSpecialRequests());
                        order.setCreatedAt(LocalDateTime.now());

                        return orderRepositoryPort.save(order)
                            .flatMap(savedOrder -> {
                                table.setStatus(TableStatus.OCCUPIED);
                                return tableRepositoryPort.save(table)
                                    .thenReturn(savedOrder);
                            })
                            .doOnSuccess(savedOrder -> {
                                log.info("Publishing ORDER_CREATED event for order: {}", savedOrder.getId());
                                orderEventPublisher.publishOrderCreated(savedOrder, Collections.emptyList());
                            })
                            .flatMap(this::toDto);
                    });
            });
    }

    @Override
    public Mono<OrderDto> getOrderById(Long id) {
        return orderRepositoryPort.getById(id)
            .flatMap(this::toDto);
    }

    @Override
    public Flux<OrderDto> getAllOrders() {
        return orderRepositoryPort.findAll()
            .flatMap(this::toDto);
    }

    @Override
    @Transactional
    public Mono<OrderDto> addItemToOrder(Long orderId, OrderItemDto itemDto) {
        return orderRepositoryPort.getById(orderId)
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

                    return orderItemRepositoryPort.save(item)
                        .flatMap(savedItem -> {
                            BigDecimal itemTotal = savedItem.getPrice()
                                .multiply(BigDecimal.valueOf(savedItem.getQuantity()));
                            order.setTotalAmount(order.getTotalAmount().add(itemTotal));
                            return orderRepositoryPort.save(order);
                        });
                }))
            .flatMap(this::toDto);
    }

    @Override
    @Transactional
    public Mono<Void> removeItemFromOrder(Long orderId, Long itemId) {
        return orderRepositoryPort.getById(orderId)
            .flatMap(order -> orderItemRepositoryPort.getById(itemId)
                .flatMap(item -> {
                    BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    order.setTotalAmount(order.getTotalAmount().subtract(itemTotal));
                    return orderRepositoryPort.save(order)
                        .then(orderItemRepositoryPort.deleteById(itemId));
                }));
    }

    @Override
    @Transactional
    public Mono<OrderDto> sendToKitchen(Long orderId) {
        return orderRepositoryPort.getById(orderId)
            .flatMap(order -> {
                if (order.getStatus() != OrderStatus.CREATED) {
                    return Mono.error(new BusinessConflictException(
                        "Cannot send to kitchen: order must be in CREATED status",
                        "Order",
                        orderId,
                        "Current status: " + order.getStatus()
                    ));
                }

                return orderItemRepositoryPort.findByOrderId(orderId)
                    .collectList()
                    .flatMap(items -> {
                        if (items.isEmpty()) {
                            return Mono.error(new BusinessConflictException(
                                "Cannot send to kitchen: order has no items",
                                "Order",
                                orderId,
                                "Items count: 0"
                            ));
                        }

                        order.setStatus(OrderStatus.IN_KITCHEN);
                        return orderRepositoryPort.save(order)
                            .doOnSuccess(savedOrder -> {
                                log.info("Publishing ORDER_SENT_TO_KITCHEN event for order: {}", savedOrder.getId());
                                orderEventPublisher.publishOrderSentToKitchen(savedOrder, items);
                            });
                    })
                    .flatMap(this::toDto);
            });
    }

    @Override
    @Transactional
    public Mono<OrderDto> closeOrder(Long orderId) {
        return orderRepositoryPort.getById(orderId)
            .flatMap(order -> {
                order.setStatus(OrderStatus.CLOSED);
                order.setClosedAt(LocalDateTime.now());
                return orderRepositoryPort.save(order)
                    .flatMap(savedOrder -> tableRepositoryPort.findById(order.getTableId())
                        .flatMap(table -> {
                            table.setStatus(TableStatus.FREE);
                            return tableRepositoryPort.save(table);
                        })
                        .thenReturn(savedOrder))
                    .flatMap(this::toDto);
            });
    }

    private Mono<OrderDto> toDto(Order order) {
        return orderItemRepositoryPort.findByOrderId(order.getId())
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

    @Override
    public Mono<Page<OrderDto>> getAllOrdersPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.DESC, "id"));

        return orderRepositoryPort.count()
            .flatMap(total -> orderRepositoryPort.findAll(pageable)
                .flatMap(this::toDto)
                .collectList()
                .map(orders -> new PageImpl<>(orders, pageable, total)));
    }

    @Override
    public Mono<Slice<OrderDto>> getAllOrdersSlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size + 1, Sort.by(Sort.Direction.DESC, "id"));

        return orderRepositoryPort.findAll(pageable)
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

    @Override
    public Mono<Page<TableDto>> getAllTablesPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "id"));

        return tableRepositoryPort.count()
            .flatMap(total -> tableRepositoryPort.findAll(pageable)
                .map(this::toTableDto)
                .collectList()
                .map(tables -> new PageImpl<>(tables, pageable, total)));
    }

    @Override
    public Mono<Page<EmployeeDto>> getAllEmployeesPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "id"));

        return employeeRepositoryPort.count()
            .flatMap(total -> employeeRepositoryPort.findAll(pageable)
                .map(this::toEmployeeDto)
                .collectList()
                .map(employees -> new PageImpl<>(employees, pageable, total)));
    }

    @Override
    @Transactional
    public Mono<EmployeeDto> createEmployee(EmployeeDto dto) {
        Employee employee = new Employee();
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setRole(dto.getRole());

        return employeeRepositoryPort.save(employee)
            .map(this::toEmployeeDto);
    }

    @Override
    @Transactional
    public Mono<TableDto> createTable(TableDto dto) {
        RestaurantTable table = new RestaurantTable();
        table.setTableNumber(dto.getTableNumber());
        table.setCapacity(dto.getCapacity());
        table.setLocation(dto.getLocation());
        table.setStatus(dto.getStatus() != null ? dto.getStatus() : TableStatus.FREE);

        return tableRepositoryPort.save(table)
            .map(this::toTableDto);
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
