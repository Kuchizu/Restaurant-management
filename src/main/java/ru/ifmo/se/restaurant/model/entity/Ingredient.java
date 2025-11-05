package ru.ifmo.se.restaurant.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ingredient name cannot be blank")
    @Size(min = 1, max = 100, message = "Ingredient name must be between 1 and 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 50, message = "Unit cannot exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String unit;

    @ManyToMany(mappedBy = "ingredients")
    private List<Dish> dishes = new ArrayList<>();

    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inventory> inventories = new ArrayList<>();

    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplyOrderIngredient> supplyOrderIngredients = new ArrayList<>();
}

