package ru.ifmo.se.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ingredient data transfer object")
public class IngredientDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Ingredient unique identifier", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotBlank(message = "Ingredient name cannot be blank")
    @Size(min = 1, max = 100, message = "Ingredient name must be between 1 and 100 characters")
    @Schema(description = "Ingredient name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Tomatoes")
    private String name;

    @Size(max = 50, message = "Unit cannot exceed 50 characters")
    @Schema(description = "Unit of measurement", example = "kg")
    private String unit;
}
