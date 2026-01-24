package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.ifmo.se.restaurant.inventory.domain.entity.Inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private IngredientJpaEntity ingredient;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minQuantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxQuantity;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public static InventoryJpaEntity fromDomain(Inventory domain, IngredientJpaEntity ingredientEntity) {
        return InventoryJpaEntity.builder()
            .id(domain.getId())
            .ingredient(ingredientEntity)
            .quantity(domain.getQuantity())
            .minQuantity(domain.getMinQuantity())
            .maxQuantity(domain.getMaxQuantity())
            .lastUpdated(domain.getLastUpdated())
            .build();
    }

    public Inventory toDomain() {
        return Inventory.builder()
            .id(id)
            .ingredient(ingredient.toDomain())
            .quantity(quantity)
            .minQuantity(minQuantity)
            .maxQuantity(maxQuantity)
            .lastUpdated(lastUpdated)
            .build();
    }
}
