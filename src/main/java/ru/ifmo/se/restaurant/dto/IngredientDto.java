package ru.ifmo.se.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDto {
    private Long id;

    @NotBlank(message = "Ingredient name cannot be blank")
    @Size(min = 1, max = 100, message = "Ingredient name must be between 1 and 100 characters")
    private String name;

    @Size(max = 50, message = "Unit cannot exceed 50 characters")
    private String unit;
}

