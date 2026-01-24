package ru.ifmo.se.restaurant.inventory.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.inventory.domain.entity.Ingredient;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ингредиент на складе")
public class IngredientDto {
    @Schema(description = "ID ингредиента (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "8")
    private Long id;

    @NotBlank(message = "Ingredient name cannot be blank")
    @Size(max = 100, message = "Ingredient name must not exceed 100 characters")
    @Schema(description = "Название ингредиента", required = true, example = "Говядина мраморная")
    private String name;

    @NotBlank(message = "Unit cannot be blank")
    @Size(max = 50, message = "Unit must not exceed 50 characters")
    @Schema(description = "Единица измерения", required = true, example = "кг")
    private String unit;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Описание ингредиента", example = "Мраморная говядина высшего сорта для стейков")
    private String description;

    public static IngredientDto fromDomain(Ingredient ingredient) {
        return IngredientDto.builder()
            .id(ingredient.getId())
            .name(ingredient.getName())
            .unit(ingredient.getUnit())
            .description(ingredient.getDescription())
            .build();
    }
}
