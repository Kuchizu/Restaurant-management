package ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.ifmo.se.restaurant.menu.domain.entity.Category;
import ru.ifmo.se.restaurant.menu.domain.entity.Dish;
import ru.ifmo.se.restaurant.menu.domain.entity.Ingredient;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "dishes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Dish name cannot be blank")
    @Size(max = 200, message = "Dish name must not exceed 200 characters")
    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Cost must be non-negative")
    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Category cannot be null")
    private CategoryJpaEntity category;

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "dish_ingredients",
        joinColumns = @JoinColumn(name = "dish_id"),
        inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private Set<IngredientJpaEntity> ingredients = new HashSet<>();

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    public static DishJpaEntity fromDomain(Dish domain, CategoryJpaEntity categoryEntity, Set<IngredientJpaEntity> ingredientEntities) {
        return DishJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .cost(domain.getCost())
                .category(categoryEntity)
                .isActive(domain.getIsActive())
                .ingredients(ingredientEntities)
                .imageUrl(domain.getImageUrl())
                .build();
    }

    public Dish toDomain() {
        Category categoryDomain = category != null ? category.toDomain() : null;
        Set<Ingredient> ingredientsDomain = ingredients.stream()
                .map(IngredientJpaEntity::toDomain)
                .collect(Collectors.toSet());

        return Dish.builder()
                .id(id)
                .name(name)
                .description(description)
                .price(price)
                .cost(cost)
                .category(categoryDomain)
                .isActive(isActive)
                .ingredients(ingredientsDomain)
                .imageUrl(imageUrl)
                .build();
    }
}
