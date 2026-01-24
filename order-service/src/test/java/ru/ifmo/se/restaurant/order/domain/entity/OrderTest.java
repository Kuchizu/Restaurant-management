package ru.ifmo.se.restaurant.order.domain.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.order.domain.valueobject.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void constructor_ShouldCreateOrder() {
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order(
                1L,
                5L,
                10L,
                OrderStatus.CREATED,
                new BigDecimal("100.00"),
                "No onions",
                now,
                null,
                1L
        );

        assertEquals(1L, order.getId());
        assertEquals(5L, order.getTableId());
        assertEquals(10L, order.getWaiterId());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(new BigDecimal("100.00"), order.getTotalAmount());
        assertEquals("No onions", order.getSpecialRequests());
        assertEquals(now, order.getCreatedAt());
        assertNull(order.getClosedAt());
        assertEquals(1L, order.getVersion());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyOrder() {
        Order order = new Order();
        assertNull(order.getId());
        assertNull(order.getStatus());
    }

    @Test
    void setters_ShouldUpdateValues() {
        Order order = new Order();
        LocalDateTime now = LocalDateTime.now();

        order.setId(1L);
        order.setTableId(5L);
        order.setWaiterId(10L);
        order.setStatus(OrderStatus.IN_KITCHEN);
        order.setTotalAmount(new BigDecimal("50.00"));
        order.setSpecialRequests("Extra sauce");
        order.setCreatedAt(now);
        order.setClosedAt(now.plusHours(1));
        order.setVersion(2L);

        assertEquals(1L, order.getId());
        assertEquals(5L, order.getTableId());
        assertEquals(10L, order.getWaiterId());
        assertEquals(OrderStatus.IN_KITCHEN, order.getStatus());
        assertEquals(new BigDecimal("50.00"), order.getTotalAmount());
        assertEquals("Extra sauce", order.getSpecialRequests());
        assertEquals(now, order.getCreatedAt());
        assertEquals(now.plusHours(1), order.getClosedAt());
        assertEquals(2L, order.getVersion());
    }

    @Test
    void equals_ShouldReturnTrue_ForSameValues() {
        LocalDateTime now = LocalDateTime.now();
        Order order1 = new Order(1L, 5L, 10L, OrderStatus.CREATED, BigDecimal.TEN, null, now, null, 1L);
        Order order2 = new Order(1L, 5L, 10L, OrderStatus.CREATED, BigDecimal.TEN, null, now, null, 1L);

        assertEquals(order1, order2);
        assertEquals(order1.hashCode(), order2.hashCode());
    }

    @Test
    void equals_ShouldReturnFalse_ForDifferentValues() {
        LocalDateTime now = LocalDateTime.now();
        Order order1 = new Order(1L, 5L, 10L, OrderStatus.CREATED, BigDecimal.TEN, null, now, null, 1L);
        Order order2 = new Order(2L, 5L, 10L, OrderStatus.CREATED, BigDecimal.TEN, null, now, null, 1L);

        assertNotEquals(order1, order2);
    }

    @Test
    void toString_ShouldReturnString() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);

        String str = order.toString();
        assertNotNull(str);
        assertTrue(str.contains("Order"));
    }

    @Test
    void status_ShouldHandleAllValues() {
        Order order = new Order();

        for (OrderStatus status : OrderStatus.values()) {
            order.setStatus(status);
            assertEquals(status, order.getStatus());
        }
    }
}
