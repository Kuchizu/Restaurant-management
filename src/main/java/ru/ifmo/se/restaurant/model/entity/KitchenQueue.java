package ru.ifmo.se.restaurant.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ifmo.se.restaurant.model.DishStatus;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "kitchen_queue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KitchenQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Order cannot be null")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    @NotNull(message = "Order item cannot be null")
    private OrderItem orderItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DishStatus status = DishStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}

