package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.ifmo.se.restaurant.inventory.domain.entity.SupplyOrder;
import ru.ifmo.se.restaurant.inventory.domain.valueobject.SupplyOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "supply_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrderJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private SupplierJpaEntity supplier;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupplyOrderStatus status;

    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "supplyOrder", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplyOrderIngredientJpaEntity> items = new ArrayList<>();

    public static SupplyOrderJpaEntity fromDomain(SupplyOrder domain, SupplierJpaEntity supplierEntity) {
        return SupplyOrderJpaEntity.builder()
            .id(domain.getId())
            .supplier(supplierEntity)
            .orderDate(domain.getOrderDate())
            .deliveryDate(domain.getDeliveryDate())
            .status(domain.getStatus())
            .totalCost(domain.getTotalCost())
            .notes(domain.getNotes())
            .build();
    }

    public SupplyOrder toDomain() {
        return SupplyOrder.builder()
            .id(id)
            .supplier(supplier.toDomain())
            .orderDate(orderDate)
            .deliveryDate(deliveryDate)
            .status(status)
            .totalCost(totalCost)
            .notes(notes)
            .items(items != null ?
                items.stream()
                    .map(SupplyOrderIngredientJpaEntity::toDomain)
                    .collect(Collectors.toList()) : null)
            .build();
    }
}
