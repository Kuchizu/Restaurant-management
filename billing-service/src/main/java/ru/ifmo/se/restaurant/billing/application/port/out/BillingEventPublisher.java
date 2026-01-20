package ru.ifmo.se.restaurant.billing.application.port.out;

import ru.ifmo.se.restaurant.billing.domain.entity.Bill;

public interface BillingEventPublisher {
    void publishBillGenerated(Bill bill);
    void publishBillPaid(Bill bill);
}
