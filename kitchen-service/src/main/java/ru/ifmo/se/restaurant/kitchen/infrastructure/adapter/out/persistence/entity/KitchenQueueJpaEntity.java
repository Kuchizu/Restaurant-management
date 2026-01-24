package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "kitchen_queue")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenQueueJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Order ID cannot be null")
    @Column(nullable = false)
    private Long orderId;

    @NotNull(message = "Order item ID cannot be null")
    @Column(nullable = false)
    private Long orderItemId;

    @Column(length = 200)
    private String dishName;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DishStatus status = DishStatus.PENDING;

    @Column(length = 500)
    private String specialRequest;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public static KitchenQueueJpaEntity fromDomain(KitchenQueue domain) {
        return KitchenQueueJpaEntity.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .orderItemId(domain.getOrderItemId())
                .dishName(domain.getDishName())
                .quantity(domain.getQuantity())
                .status(domain.getStatus())
                .specialRequest(domain.getSpecialRequest())
                .createdAt(domain.getCreatedAt())
                .startedAt(domain.getStartedAt())
                .completedAt(domain.getCompletedAt())
                .build();
    }

    public KitchenQueue toDomain() {
        return KitchenQueue.builder()
                .id(id)
                .orderId(orderId)
                .orderItemId(orderItemId)
                .dishName(dishName)
                .quantity(quantity)
                .status(status)
                .specialRequest(specialRequest)
                .createdAt(createdAt)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .build();
    }
}
