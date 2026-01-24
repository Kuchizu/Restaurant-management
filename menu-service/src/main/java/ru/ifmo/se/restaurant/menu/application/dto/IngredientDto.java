package ru.ifmo.se.restaurant.menu.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ингредиент для блюда")
public class IngredientDto {
    @Schema(description = "ID ингредиента (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "8")
    private Long id;

    @NotBlank(message = "Ingredient name cannot be blank")
    @Size(max = 100, message = "Ingredient name must not exceed 100 characters")
    @Schema(description = "Название ингредиента", required = true, example = "Говядина мраморная")
    private String name;

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    @Schema(description = "Единица измерения", example = "кг")
    private String unit;
}
