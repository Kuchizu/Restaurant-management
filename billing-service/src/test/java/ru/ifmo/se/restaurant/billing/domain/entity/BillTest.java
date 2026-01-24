package ru.ifmo.se.restaurant.billing.domain.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;
import ru.ifmo.se.restaurant.billing.domain.valueobject.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class BillTest {

    @Test
    void builder_ShouldCreateBill() {
        Bill bill = Bill.builder()
                .id(1L)
                .orderId(100L)
                .totalAmount(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .serviceCharge(new BigDecimal("5.00"))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("115.00"))
                .status(BillStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        assertEquals(1L, bill.getId());
        assertEquals(100L, bill.getOrderId());
        assertEquals(BillStatus.PENDING, bill.getStatus());
    }

    @Test
    void isPaid_ShouldReturnTrue_WhenStatusIsPaid() {
        Bill bill = Bill.builder().status(BillStatus.PAID).build();
        assertTrue(bill.isPaid());
        assertFalse(bill.isPending());
    }

    @Test
    void isPending_ShouldReturnTrue_WhenStatusIsPending() {
        Bill bill = Bill.builder().status(BillStatus.PENDING).build();
        assertTrue(bill.isPending());
        assertFalse(bill.isPaid());
    }

    @Test
    void isCancelled_ShouldReturnTrue_WhenStatusIsCancelled() {
        Bill bill = Bill.builder().status(BillStatus.CANCELLED).build();
        assertTrue(bill.isCancelled());
    }

    @Test
    void canApplyDiscount_ShouldReturnTrue_WhenPending() {
        Bill pending = Bill.builder().status(BillStatus.PENDING).build();
        Bill paid = Bill.builder().status(BillStatus.PAID).build();
        assertTrue(pending.canApplyDiscount());
        assertFalse(paid.canApplyDiscount());
    }

    @Test
    void canBePaid_ShouldReturnTrue_WhenPending() {
        Bill pending = Bill.builder().status(BillStatus.PENDING).build();
        Bill cancelled = Bill.builder().status(BillStatus.CANCELLED).build();
        assertTrue(pending.canBePaid());
        assertFalse(cancelled.canBePaid());
    }

    @Test
    void canBeCancelled_ShouldReturnTrue_WhenNotPaid() {
        Bill pending = Bill.builder().status(BillStatus.PENDING).build();
        Bill paid = Bill.builder().status(BillStatus.PAID).build();
        assertTrue(pending.canBeCancelled());
        assertFalse(paid.canBeCancelled());
    }
}
