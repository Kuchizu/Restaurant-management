package ru.ifmo.se.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Dish data transfer object")
public class DishDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Dish unique identifier", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotBlank(message = "Dish name cannot be blank")
    @Size(min = 1, max = 100, message = "Dish name must be between 1 and 100 characters")
    @Schema(description = "Dish name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Caesar Salad")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Dish description", example = "Fresh romaine lettuce with Caesar dressing")
    private String description;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    @Schema(description = "Dish price", requiredMode = Schema.RequiredMode.REQUIRED, example = "12.99")
    private BigDecimal price;

    @NotNull(message = "Cost cannot be null")
    @Positive(message = "Cost must be positive")
    @Schema(description = "Dish cost", requiredMode = Schema.RequiredMode.REQUIRED, example = "5.50")
    private BigDecimal cost;

    @NotNull(message = "Category ID cannot be null")
    @Schema(description = "Category ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long categoryId;

    @Schema(description = "Category name", accessMode = Schema.AccessMode.READ_ONLY, example = "Salads")
    private String categoryName;

    @Schema(description = "List of ingredient IDs", example = "[1, 2, 3]")
    private List<Long> ingredientIds;

    @Schema(description = "Whether dish is active", accessMode = Schema.AccessMode.READ_ONLY, example = "true")
    private Boolean isActive;
}
