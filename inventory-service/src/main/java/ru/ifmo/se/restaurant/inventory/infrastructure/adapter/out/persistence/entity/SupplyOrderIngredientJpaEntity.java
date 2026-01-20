package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.ifmo.se.restaurant.inventory.domain.entity.SupplyOrderItem;

import java.math.BigDecimal;

@Entity
@Table(name = "supply_order_ingredients")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrderIngredientJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_order_id", nullable = false)
    private SupplyOrderJpaEntity supplyOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private IngredientJpaEntity ingredient;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    public static SupplyOrderIngredientJpaEntity fromDomain(SupplyOrderItem domain,
                                                            SupplyOrderJpaEntity orderEntity,
                                                            IngredientJpaEntity ingredientEntity) {
        return SupplyOrderIngredientJpaEntity.builder()
            .id(domain.getId())
            .supplyOrder(orderEntity)
            .ingredient(ingredientEntity)
            .quantity(domain.getQuantity())
            .unitPrice(domain.getUnitPrice())
            .build();
    }

    public SupplyOrderItem toDomain() {
        return SupplyOrderItem.builder()
            .id(id)
            .ingredient(ingredient.toDomain())
            .quantity(quantity)
            .unitPrice(unitPrice)
            .build();
    }
}
