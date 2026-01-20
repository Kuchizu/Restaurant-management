package ru.ifmo.se.restaurant.common.event.billing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillGeneratedEvent {
    private Long billId;
    private Long orderId;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal serviceCharge;
    private BigDecimal totalAmount;
    private Instant generatedAt;
}
