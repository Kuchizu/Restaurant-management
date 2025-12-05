package ru.ifmo.se.restaurant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dish popularity statistics")
public class DishPopularityDto {
    @Schema(description = "Dish ID", example = "1")
    private Long dishId;

    @Schema(description = "Dish name", example = "Caesar Salad")
    private String dishName;

    @Schema(description = "Total quantity ordered", example = "150")
    private Integer quantity;
}
