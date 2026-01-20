package ru.ifmo.se.restaurant.billing.application.port.in;

import ru.ifmo.se.restaurant.billing.application.dto.BillDto;
import ru.ifmo.se.restaurant.billing.domain.valueobject.PaymentMethod;

import java.math.BigDecimal;

public interface UpdateBillUseCase {
    BillDto applyDiscount(Long billId, BigDecimal discountAmount);
    BillDto payBill(Long billId, PaymentMethod paymentMethod);
    BillDto cancelBill(Long billId);
}
