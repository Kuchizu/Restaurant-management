package ru.ifmo.se.restaurant.menu.dto;

import jakarta.validation.constraints.*;
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
    @Size(max = 200, message = "Dish name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Cost must be non-negative")
    private BigDecimal cost;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    private String categoryName;
    private Boolean isActive;
    private List<Long> ingredientIds;
}
