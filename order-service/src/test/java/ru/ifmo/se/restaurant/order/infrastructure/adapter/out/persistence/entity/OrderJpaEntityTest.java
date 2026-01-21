package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.order.domain.valueobject.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderJpaEntityTest {

    @Test
    void constructor_ShouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        OrderJpaEntity entity = new OrderJpaEntity(
                1L, 2L, 3L, OrderStatus.CREATED,
                new BigDecimal("100.00"), "No onions",
                now, null, 0L
        );

        assertEquals(1L, entity.getId());
        assertEquals(2L, entity.getTableId());
        assertEquals(3L, entity.getWaiterId());
        assertEquals(OrderStatus.CREATED, entity.getStatus());
        assertEquals(new BigDecimal("100.00"), entity.getTotalAmount());
        assertEquals("No onions", entity.getSpecialRequests());
        assertEquals(now, entity.getCreatedAt());
        assertNull(entity.getClosedAt());
        assertEquals(0L, entity.getVersion());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        OrderJpaEntity entity = new OrderJpaEntity();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime closedAt = createdAt.plusHours(2);

        entity.setId(1L);
        entity.setTableId(5L);
        entity.setWaiterId(10L);
        entity.setStatus(OrderStatus.CLOSED);
        entity.setTotalAmount(new BigDecimal("250.50"));
        entity.setSpecialRequests("Extra cheese");
        entity.setCreatedAt(createdAt);
        entity.setClosedAt(closedAt);
        entity.setVersion(1L);

        assertEquals(1L, entity.getId());
        assertEquals(5L, entity.getTableId());
        assertEquals(10L, entity.getWaiterId());
        assertEquals(OrderStatus.CLOSED, entity.getStatus());
        assertEquals(new BigDecimal("250.50"), entity.getTotalAmount());
        assertEquals("Extra cheese", entity.getSpecialRequests());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(closedAt, entity.getClosedAt());
        assertEquals(1L, entity.getVersion());
    }

    @Test
    void defaultConstructor_ShouldCreateEmptyEntity() {
        OrderJpaEntity entity = new OrderJpaEntity();
        assertNull(entity.getId());
        assertNull(entity.getTableId());
        assertNull(entity.getStatus());
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        OrderJpaEntity entity1 = new OrderJpaEntity(1L, 2L, 3L, OrderStatus.CREATED,
                new BigDecimal("100.00"), null, now, null, 0L);
        OrderJpaEntity entity2 = new OrderJpaEntity(1L, 2L, 3L, OrderStatus.CREATED,
                new BigDecimal("100.00"), null, now, null, 0L);

        assertEquals(entity1, entity2);
    }
}
