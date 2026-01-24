package ru.ifmo.se.restaurant.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Позиция заказа (блюдо)")
public class OrderItemDto {
    @Schema(description = "ID позиции (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotNull(message = "Dish ID cannot be null")
    @Schema(description = "ID блюда из меню", required = true, example = "12")
    private Long dishId;

    @Schema(description = "Название блюда (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "Цезарь с курицей")
    private String dishName;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Количество порций", required = true, example = "2")
    private Integer quantity;

    @Schema(description = "Цена за единицу (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "450.00")
    private BigDecimal price;

    @Schema(description = "Особые пожелания к блюду", example = "Без майонеза, добавить острый соус")
    private String specialRequest;
}
