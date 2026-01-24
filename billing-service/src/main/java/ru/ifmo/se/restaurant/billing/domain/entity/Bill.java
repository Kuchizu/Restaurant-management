package ru.ifmo.se.restaurant.billing.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;
import ru.ifmo.se.restaurant.billing.domain.valueobject.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Bill {
    private final Long id;
    private final Long orderId;
    private final BigDecimal totalAmount;
    private final BigDecimal taxAmount;
    private final BigDecimal serviceCharge;
    private final BigDecimal discountAmount;
    private final BigDecimal finalAmount;
    private final BillStatus status;
    private final PaymentMethod paymentMethod;
    private final LocalDateTime createdAt;
    private final LocalDateTime paidAt;
    private final String notes;

    public boolean isPaid() {
        return status == BillStatus.PAID;
    }

    public boolean isPending() {
        return status == BillStatus.PENDING;
    }

    public boolean isCancelled() {
        return status == BillStatus.CANCELLED;
    }

    public boolean canApplyDiscount() {
        return status == BillStatus.PENDING;
    }

    public boolean canBePaid() {
        return status == BillStatus.PENDING;
    }

    public boolean canBeCancelled() {
        return status != BillStatus.PAID;
    }
}
