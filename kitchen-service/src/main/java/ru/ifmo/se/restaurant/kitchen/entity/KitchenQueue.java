package ru.ifmo.se.restaurant.kitchen.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "kitchen_queue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KitchenQueue {
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
    private Integer quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DishStatus status = DishStatus.PENDING;

    @Column(length = 500)
    private String specialRequest;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
