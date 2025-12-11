package ru.ifmo.se.restaurant.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Блюдо из меню")
public class DishDto {
    @Schema(description = "ID блюда (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "12")
    private Long id;

    @NotBlank(message = "Dish name cannot be blank")
    @Size(max = 200, message = "Dish name must not exceed 200 characters")
    @Schema(description = "Название блюда", required = true, example = "Стейк Рибай медиум")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Описание блюда", example = "Сочный стейк из мраморной говядины с пряными травами")
    private String description;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Schema(description = "Цена блюда", required = true, example = "2450.00")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Cost must be non-negative")
    @Schema(description = "Себестоимость блюда", example = "980.00")
    private BigDecimal cost;

    @NotNull(message = "Category ID cannot be null")
    @Schema(description = "ID категории", required = true, example = "3")
    private Long categoryId;

    @Schema(description = "Название категории (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "Горячие блюда")
    private String categoryName;

    @Schema(description = "Доступно ли блюдо для заказа", example = "true")
    private Boolean isActive;

    @Schema(description = "Список ID ингредиентов", example = "[1, 5, 8, 12]")
    private List<Long> ingredientIds;
}
