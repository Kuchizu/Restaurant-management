package ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.billing.domain.entity.Bill;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;
import ru.ifmo.se.restaurant.billing.domain.valueobject.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BillJpaEntityTest {

    @Test
    void fromDomain_ShouldMapAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Bill domain = Bill.builder()
                .id(1L)
                .orderId(100L)
                .totalAmount(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .serviceCharge(new BigDecimal("5.00"))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("115.00"))
                .status(BillStatus.PENDING)
                .createdAt(now)
                .build();

        BillJpaEntity entity = BillJpaEntity.fromDomain(domain);

        assertEquals(1L, entity.getId());
        assertEquals(100L, entity.getOrderId());
        assertEquals(new BigDecimal("100.00"), entity.getTotalAmount());
        assertEquals(BillStatus.PENDING, entity.getStatus());
    }

    @Test
    void toDomain_ShouldMapAllFields() {
        LocalDateTime now = LocalDateTime.now();
        BillJpaEntity entity = BillJpaEntity.builder()
                .id(1L)
                .orderId(100L)
                .totalAmount(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .serviceCharge(new BigDecimal("5.00"))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("115.00"))
                .status(BillStatus.PAID)
                .paymentMethod(PaymentMethod.CASH)
                .createdAt(now)
                .paidAt(now.plusMinutes(30))
                .notes("Test note")
                .build();

        Bill domain = entity.toDomain();

        assertEquals(1L, domain.getId());
        assertEquals(100L, domain.getOrderId());
        assertEquals(BillStatus.PAID, domain.getStatus());
        assertEquals(PaymentMethod.CASH, domain.getPaymentMethod());
        assertEquals("Test note", domain.getNotes());
    }

    @Test
    void fromDomain_AndToDomain_ShouldBeSymmetric() {
        LocalDateTime now = LocalDateTime.now();
        Bill original = Bill.builder()
                .id(1L)
                .orderId(100L)
                .totalAmount(new BigDecimal("100.00"))
                .finalAmount(new BigDecimal("100.00"))
                .status(BillStatus.PENDING)
                .createdAt(now)
                .build();

        BillJpaEntity entity = BillJpaEntity.fromDomain(original);
        Bill result = entity.toDomain();

        assertEquals(original.getId(), result.getId());
        assertEquals(original.getOrderId(), result.getOrderId());
        assertEquals(original.getStatus(), result.getStatus());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        BillJpaEntity entity = new BillJpaEntity();
        entity.setId(1L);
        entity.setOrderId(100L);
        entity.setTotalAmount(new BigDecimal("50.00"));
        entity.setStatus(BillStatus.CANCELLED);

        assertEquals(1L, entity.getId());
        assertEquals(100L, entity.getOrderId());
        assertEquals(new BigDecimal("50.00"), entity.getTotalAmount());
        assertEquals(BillStatus.CANCELLED, entity.getStatus());
    }
}
