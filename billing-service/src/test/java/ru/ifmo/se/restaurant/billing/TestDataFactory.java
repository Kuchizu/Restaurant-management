package ru.ifmo.se.restaurant.billing;

import ru.ifmo.se.restaurant.billing.dto.BillDto;
import ru.ifmo.se.restaurant.billing.dto.OrderDto;
import ru.ifmo.se.restaurant.billing.entity.Bill;
import ru.ifmo.se.restaurant.billing.entity.BillStatus;
import ru.ifmo.se.restaurant.billing.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TestDataFactory {

    public static Bill createMockBill(Long id) {
        Bill bill = new Bill();
        bill.setId(id);
        bill.setOrderId(100L);
        bill.setTotalAmount(new BigDecimal("100.00"));
        bill.setTaxAmount(new BigDecimal("10.00"));
        bill.setServiceCharge(new BigDecimal("5.00"));
        bill.setDiscountAmount(BigDecimal.ZERO);
        bill.setFinalAmount(new BigDecimal("115.00"));
        bill.setStatus(BillStatus.PENDING);
        bill.setCreatedAt(LocalDateTime.now());
        return bill;
    }

    public static BillDto createMockBillDto(Long id) {
        BillDto dto = new BillDto();
        dto.setId(id);
        dto.setOrderId(100L);
        dto.setTotalAmount(new BigDecimal("100.00"));
        dto.setTaxAmount(new BigDecimal("10.00"));
        dto.setServiceCharge(new BigDecimal("5.00"));
        dto.setDiscountAmount(BigDecimal.ZERO);
        dto.setFinalAmount(new BigDecimal("115.00"));
        dto.setStatus(BillStatus.PENDING);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }

    public static OrderDto createMockOrderDto() {
        OrderDto dto = new OrderDto();
        dto.setId(100L);
        dto.setTotalAmount(new BigDecimal("100.00"));
        return dto;
    }

    public static Bill createBillWithStatus(Long id, BillStatus status) {
        Bill bill = createMockBill(id);
        bill.setStatus(status);
        if (status == BillStatus.PAID) {
            bill.setPaymentMethod(PaymentMethod.CASH);
            bill.setPaidAt(LocalDateTime.now());
        }
        return bill;
    }
}
