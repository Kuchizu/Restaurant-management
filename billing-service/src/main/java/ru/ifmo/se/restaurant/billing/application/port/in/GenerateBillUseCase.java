package ru.ifmo.se.restaurant.billing.application.port.in;

import ru.ifmo.se.restaurant.billing.application.dto.BillDto;

public interface GenerateBillUseCase {
    BillDto generateBill(Long orderId);
}
