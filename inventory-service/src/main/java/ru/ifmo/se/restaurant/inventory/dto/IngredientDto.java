package ru.ifmo.se.restaurant.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ингредиент на складе")
public class IngredientDto {
    @Schema(description = "ID ингредиента (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "8")
    private Long id;

    @Schema(description = "Название ингредиента", required = true, example = "Говядина мраморная")
    private String name;

    @Schema(description = "Единица измерения", required = true, example = "кг")
    private String unit;

    @Schema(description = "Описание ингредиента", example = "Мраморная говядина высшего сорта для стейков")
    private String description;
}
