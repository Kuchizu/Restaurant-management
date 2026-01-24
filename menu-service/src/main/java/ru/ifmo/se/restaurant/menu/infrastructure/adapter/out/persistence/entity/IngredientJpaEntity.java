package ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.ifmo.se.restaurant.menu.domain.entity.Ingredient;

import java.util.HashSet;
import java.util.Set;

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

    @NotBlank(message = "Ingredient name cannot be blank")
    @Size(max = 100, message = "Ingredient name must not exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    @Column(length = 50)
    private String unit;

    @ManyToMany(mappedBy = "ingredients")
    private Set<DishJpaEntity> dishes = new HashSet<>();

    public static IngredientJpaEntity fromDomain(Ingredient domain) {
        return IngredientJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .unit(domain.getUnit())
                .build();
    }

    public Ingredient toDomain() {
        return Ingredient.builder()
                .id(id)
                .name(name)
                .unit(unit)
                .build();
    }
}
