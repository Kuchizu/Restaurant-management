package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.ifmo.se.restaurant.inventory.domain.entity.Ingredient;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 50)
    private String unit;

    @Column(length = 500)
    private String description;

    public static IngredientJpaEntity fromDomain(Ingredient domain) {
        return IngredientJpaEntity.builder()
            .id(domain.getId())
            .name(domain.getName())
            .unit(domain.getUnit())
            .description(domain.getDescription())
            .build();
    }

    public Ingredient toDomain() {
        return Ingredient.builder()
            .id(id)
            .name(name)
            .unit(unit)
            .description(description)
            .build();
    }
}
