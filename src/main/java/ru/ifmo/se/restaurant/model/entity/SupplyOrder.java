package ru.ifmo.se.restaurant.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ifmo.se.restaurant.model.SupplyOrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "supply_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    @NotNull(message = "Supplier cannot be null")
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupplyOrderStatus status = SupplyOrderStatus.CREATED;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime orderedAt;
    private LocalDateTime receivedAt;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "supplyOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplyOrderIngredient> ingredients = new ArrayList<>();
}

