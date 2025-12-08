package ru.ifmo.se.restaurant.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.billing.entity.BillStatus;
import ru.ifmo.se.restaurant.billing.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillDto {
    private Long id;
    private Long orderId;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal serviceCharge;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private BillStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String notes;
}
