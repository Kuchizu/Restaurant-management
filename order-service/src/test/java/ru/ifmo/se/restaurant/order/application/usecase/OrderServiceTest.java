package ru.ifmo.se.restaurant.order.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.ifmo.se.restaurant.order.application.dto.*;
import ru.ifmo.se.restaurant.order.application.port.out.*;
import ru.ifmo.se.restaurant.order.domain.entity.*;
import ru.ifmo.se.restaurant.order.domain.exception.BusinessConflictException;
import ru.ifmo.se.restaurant.order.domain.exception.ServiceUnavailableException;
import ru.ifmo.se.restaurant.order.domain.valueobject.EmployeeRole;
import ru.ifmo.se.restaurant.order.domain.valueobject.OrderStatus;
import ru.ifmo.se.restaurant.order.domain.valueobject.TableStatus;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.in.web.client.MenuServiceClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private OrderItemRepositoryPort orderItemRepositoryPort;

    @Mock
    private TableRepositoryPort tableRepositoryPort;

    @Mock
    private EmployeeRepositoryPort employeeRepositoryPort;

    @Mock
    private MenuServiceClient menuServiceClient;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private RestaurantTable testTable;
    private Employee testEmployee;
    private OrderItem testItem;

    @BeforeEach
    void setUp() {
        testTable = new RestaurantTable();
        testTable.setId(1L);
        testTable.setTableNumber("A-1");
        testTable.setCapacity(4);
        testTable.setStatus(TableStatus.FREE);

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john@test.com");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setTableId(1L);
        testOrder.setWaiterId(1L);
        testOrder.setStatus(OrderStatus.CREATED);
        testOrder.setTotalAmount(BigDecimal.ZERO);
        testOrder.setCreatedAt(LocalDateTime.now());

        testItem = new OrderItem();
        testItem.setId(1L);
        testItem.setOrderId(1L);
        testItem.setDishId(1L);
        testItem.setDishName("Pizza");
        testItem.setQuantity(2);
        testItem.setPrice(new BigDecimal("15.00"));
    }

    @Test
    void createOrder_ShouldCreateOrderSuccessfully() {
        OrderDto dto = new OrderDto();
        dto.setTableId(1L);
        dto.setWaiterId(1L);

        when(tableRepositoryPort.getById(1L)).thenReturn(Mono.just(testTable));
        when(employeeRepositoryPort.getById(1L)).thenReturn(Mono.just(testEmployee));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(Mono.just(testOrder));
        when(tableRepositoryPort.save(any(RestaurantTable.class))).thenReturn(Mono.just(testTable));
        when(orderItemRepositoryPort.findByOrderId(anyLong())).thenReturn(Flux.empty());
        doNothing().when(orderEventPublisher).publishOrderCreated(any(), any());

        StepVerifier.create(orderService.createOrder(dto))
                .expectNextMatches(result -> result.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void createOrder_ShouldFailWhenTableOccupied() {
        testTable.setStatus(TableStatus.OCCUPIED);

        OrderDto dto = new OrderDto();
        dto.setTableId(1L);
        dto.setWaiterId(1L);

        when(tableRepositoryPort.getById(1L)).thenReturn(Mono.just(testTable));

        StepVerifier.create(orderService.createOrder(dto))
                .expectError(BusinessConflictException.class)
                .verify();
    }

    @Test
    void getOrderById_ShouldReturnOrder() {
        when(orderRepositoryPort.getById(1L)).thenReturn(Mono.just(testOrder));
        when(orderItemRepositoryPort.findByOrderId(1L)).thenReturn(Flux.just(testItem));

        StepVerifier.create(orderService.getOrderById(1L))
                .expectNextMatches(result ->
                        result.getId().equals(1L) &&
                        result.getItems().size() == 1)
                .verifyComplete();
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        when(orderRepositoryPort.findAll()).thenReturn(Flux.just(testOrder));
        when(orderItemRepositoryPort.findByOrderId(anyLong())).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getAllOrders())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void sendToKitchen_ShouldFailWhenNoItems() {
        when(orderRepositoryPort.getById(1L)).thenReturn(Mono.just(testOrder));
        when(orderItemRepositoryPort.findByOrderId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(orderService.sendToKitchen(1L))
                .expectError(BusinessConflictException.class)
                .verify();
    }

    @Test
    void sendToKitchen_ShouldFailWhenWrongStatus() {
        testOrder.setStatus(OrderStatus.IN_KITCHEN);

        when(orderRepositoryPort.getById(1L)).thenReturn(Mono.just(testOrder));

        StepVerifier.create(orderService.sendToKitchen(1L))
                .expectError(BusinessConflictException.class)
                .verify();
    }

    @Test
    void sendToKitchen_ShouldSucceedWithItems() {
        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setTableId(1L);
        updatedOrder.setWaiterId(1L);
        updatedOrder.setStatus(OrderStatus.IN_KITCHEN);
        updatedOrder.setTotalAmount(new BigDecimal("30.00"));
        updatedOrder.setCreatedAt(LocalDateTime.now());

        when(orderRepositoryPort.getById(1L)).thenReturn(Mono.just(testOrder));
        when(orderItemRepositoryPort.findByOrderId(1L)).thenReturn(Flux.just(testItem));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(Mono.just(updatedOrder));
        doNothing().when(orderEventPublisher).publishOrderSentToKitchen(any(), any());

        StepVerifier.create(orderService.sendToKitchen(1L))
                .expectNextMatches(result -> result.getStatus() == OrderStatus.IN_KITCHEN)
                .verifyComplete();
    }

    @Test
    void closeOrder_ShouldCloseOrderAndFreeTable() {
        Order closedOrder = new Order();
        closedOrder.setId(1L);
        closedOrder.setTableId(1L);
        closedOrder.setWaiterId(1L);
        closedOrder.setStatus(OrderStatus.CLOSED);
        closedOrder.setTotalAmount(new BigDecimal("30.00"));
        closedOrder.setClosedAt(LocalDateTime.now());

        when(orderRepositoryPort.getById(1L)).thenReturn(Mono.just(testOrder));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(Mono.just(closedOrder));
        when(tableRepositoryPort.findById(1L)).thenReturn(Mono.just(testTable));
        when(tableRepositoryPort.save(any(RestaurantTable.class))).thenReturn(Mono.just(testTable));
        when(orderItemRepositoryPort.findByOrderId(anyLong())).thenReturn(Flux.empty());

        StepVerifier.create(orderService.closeOrder(1L))
                .expectNextMatches(result -> result.getStatus() == OrderStatus.CLOSED)
                .verifyComplete();
    }

    @Test
    void createTable_ShouldCreateTable() {
        TableDto dto = new TableDto(null, "A-5", 4, "Window", TableStatus.FREE);

        RestaurantTable savedTable = new RestaurantTable();
        savedTable.setId(5L);
        savedTable.setTableNumber("A-5");
        savedTable.setCapacity(4);
        savedTable.setLocation("Window");
        savedTable.setStatus(TableStatus.FREE);

        when(tableRepositoryPort.save(any(RestaurantTable.class))).thenReturn(Mono.just(savedTable));

        StepVerifier.create(orderService.createTable(dto))
                .expectNextMatches(result ->
                        result.getTableNumber().equals("A-5") &&
                        result.getCapacity() == 4)
                .verifyComplete();
    }

    @Test
    void createEmployee_ShouldCreateEmployee() {
        EmployeeDto dto = new EmployeeDto(null, "Jane", "Smith", "jane@test.com", "123456789", EmployeeRole.WAITER);

        Employee savedEmployee = new Employee();
        savedEmployee.setId(2L);
        savedEmployee.setFirstName("Jane");
        savedEmployee.setLastName("Smith");
        savedEmployee.setEmail("jane@test.com");
        savedEmployee.setPhone("123456789");
        savedEmployee.setRole(EmployeeRole.WAITER);

        when(employeeRepositoryPort.save(any(Employee.class))).thenReturn(Mono.just(savedEmployee));

        StepVerifier.create(orderService.createEmployee(dto))
                .expectNextMatches(result ->
                        result.getFirstName().equals("Jane") &&
                        result.getLastName().equals("Smith"))
                .verifyComplete();
    }

    @Test
    void addItemToOrder_ShouldAddItem() {
        DishResponse dishResponse = new DishResponse(1L, "Pizza", new BigDecimal("15.00"), true);

        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setDishId(1L);
        itemDto.setQuantity(2);

        when(orderRepositoryPort.getById(1L)).thenReturn(Mono.just(testOrder));
        when(menuServiceClient.getDish(1L)).thenReturn(Mono.just(dishResponse));
        when(orderItemRepositoryPort.save(any(OrderItem.class))).thenReturn(Mono.just(testItem));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(Mono.just(testOrder));
        when(orderItemRepositoryPort.findByOrderId(1L)).thenReturn(Flux.just(testItem));

        StepVerifier.create(orderService.addItemToOrder(1L, itemDto))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void addItemToOrder_ShouldFailWhenDishPriceNull() {
        DishResponse dishResponse = new DishResponse(1L, "Pizza", null, true);

        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setDishId(1L);
        itemDto.setQuantity(2);

        when(orderRepositoryPort.getById(1L)).thenReturn(Mono.just(testOrder));
        when(menuServiceClient.getDish(1L)).thenReturn(Mono.just(dishResponse));

        StepVerifier.create(orderService.addItemToOrder(1L, itemDto))
                .expectError(ServiceUnavailableException.class)
                .verify();
    }

    @Test
    void removeItemFromOrder_ShouldRemoveItem() {
        when(orderRepositoryPort.getById(1L)).thenReturn(Mono.just(testOrder));
        when(orderItemRepositoryPort.getById(1L)).thenReturn(Mono.just(testItem));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(Mono.just(testOrder));
        when(orderItemRepositoryPort.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.removeItemFromOrder(1L, 1L))
                .verifyComplete();
    }

    @Test
    void getAllOrdersPaginated_ShouldReturnPage() {
        when(orderRepositoryPort.count()).thenReturn(Mono.just(1L));
        when(orderRepositoryPort.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(Flux.just(testOrder));
        when(orderItemRepositoryPort.findByOrderId(anyLong())).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getAllOrdersPaginated(0, 10))
                .expectNextMatches(page -> page.getContent().size() == 1)
                .verifyComplete();
    }

    @Test
    void getAllOrdersSlice_ShouldReturnSlice() {
        when(orderRepositoryPort.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(Flux.just(testOrder));
        when(orderItemRepositoryPort.findByOrderId(anyLong())).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getAllOrdersSlice(0, 10))
                .expectNextMatches(slice -> slice.getContent().size() == 1)
                .verifyComplete();
    }

    @Test
    void getAllOrdersSlice_WithMoreData_ShouldHaveNext() {
        Order order2 = new Order();
        order2.setId(2L);
        order2.setTableId(1L);
        order2.setWaiterId(1L);
        order2.setStatus(OrderStatus.CREATED);
        order2.setTotalAmount(BigDecimal.ZERO);

        when(orderRepositoryPort.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(Flux.just(testOrder, order2));
        when(orderItemRepositoryPort.findByOrderId(anyLong())).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getAllOrdersSlice(0, 1))
                .expectNextMatches(slice -> slice.hasNext() && slice.getContent().size() == 1)
                .verifyComplete();
    }

    @Test
    void getAllTablesPaginated_ShouldReturnPage() {
        when(tableRepositoryPort.count()).thenReturn(Mono.just(1L));
        when(tableRepositoryPort.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(Flux.just(testTable));

        StepVerifier.create(orderService.getAllTablesPaginated(0, 10))
                .expectNextMatches(page -> page.getContent().size() == 1)
                .verifyComplete();
    }

    @Test
    void getAllEmployeesPaginated_ShouldReturnPage() {
        when(employeeRepositoryPort.count()).thenReturn(Mono.just(1L));
        when(employeeRepositoryPort.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(Flux.just(testEmployee));

        StepVerifier.create(orderService.getAllEmployeesPaginated(0, 10))
                .expectNextMatches(page -> page.getContent().size() == 1)
                .verifyComplete();
    }

    @Test
    void createTable_WithNullStatus_ShouldDefaultToFree() {
        TableDto dto = new TableDto(null, "B-1", 2, "Corner", null);

        RestaurantTable savedTable = new RestaurantTable();
        savedTable.setId(10L);
        savedTable.setTableNumber("B-1");
        savedTable.setCapacity(2);
        savedTable.setLocation("Corner");
        savedTable.setStatus(TableStatus.FREE);

        when(tableRepositoryPort.save(any(RestaurantTable.class))).thenReturn(Mono.just(savedTable));

        StepVerifier.create(orderService.createTable(dto))
                .expectNextMatches(result -> result.getStatus() == TableStatus.FREE)
                .verifyComplete();
    }
}
