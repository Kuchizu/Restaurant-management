package ru.ifmo.se.restaurant.menu.dto;

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
    @Size(max = 100, message = "Ingredient name must not exceed 100 characters")
    private String name;

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;
}
