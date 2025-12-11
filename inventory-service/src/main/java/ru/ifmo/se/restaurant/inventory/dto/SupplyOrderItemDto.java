package ru.ifmo.se.restaurant.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Позиция заказа поставки")
public class SupplyOrderItemDto {
    @Schema(description = "ID позиции (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "42")
    private Long id;

    @Schema(description = "ID ингредиента", required = true, example = "8")
    private Long ingredientId;

    @Schema(description = "Название ингредиента (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "Говядина мраморная")
    private String ingredientName;

    @Schema(description = "Количество для заказа", required = true, example = "50.0")
    private BigDecimal quantity;

    @Schema(description = "Цена за единицу", required = true, example = "950.00")
    private BigDecimal unitPrice;
}
