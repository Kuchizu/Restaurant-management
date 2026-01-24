package ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.ifmo.se.restaurant.billing.domain.entity.Bill;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;
import ru.ifmo.se.restaurant.billing.domain.valueobject.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "service_charge", precision = 10, scale = 2)
    private BigDecimal serviceCharge;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(length = 500)
    private String notes;

    public static BillJpaEntity fromDomain(Bill domain) {
        return BillJpaEntity.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .totalAmount(domain.getTotalAmount())
                .taxAmount(domain.getTaxAmount())
                .serviceCharge(domain.getServiceCharge())
                .discountAmount(domain.getDiscountAmount())
                .finalAmount(domain.getFinalAmount())
                .status(domain.getStatus())
                .paymentMethod(domain.getPaymentMethod())
                .createdAt(domain.getCreatedAt())
                .paidAt(domain.getPaidAt())
                .notes(domain.getNotes())
                .build();
    }

    public Bill toDomain() {
        return Bill.builder()
                .id(id)
                .orderId(orderId)
                .totalAmount(totalAmount)
                .taxAmount(taxAmount)
                .serviceCharge(serviceCharge)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .status(status)
                .paymentMethod(paymentMethod)
                .createdAt(createdAt)
                .paidAt(paidAt)
                .notes(notes)
                .build();
    }
}
