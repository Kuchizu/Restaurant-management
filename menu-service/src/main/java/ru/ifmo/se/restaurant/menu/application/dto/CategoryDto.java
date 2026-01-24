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
@Schema(description = "Категория меню")
public class CategoryDto {
    @Schema(description = "ID категории (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Schema(description = "Название категории", required = true, example = "Горячие блюда")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Описание категории", example = "Основные блюда из мяса, рыбы и птицы")
    private String description;

    @Schema(description = "Активна ли категория", example = "true")
    private Boolean isActive;
}
