package ru.ifmo.se.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishDto {
    private Long id;

    @NotBlank(message = "Dish name cannot be blank")
    @Size(min = 1, max = 100, message = "Dish name must be between 1 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Cost cannot be null")
    @Positive(message = "Cost must be positive")
    private BigDecimal cost;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    private String categoryName;

    private List<Long> ingredientIds;

    private Boolean isActive;
}

