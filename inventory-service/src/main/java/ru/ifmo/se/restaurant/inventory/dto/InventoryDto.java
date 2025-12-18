package ru.ifmo.se.restaurant.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запись о складских запасах ингредиента")
public class InventoryDto {
    @Schema(description = "ID записи (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @Schema(description = "ID ингредиента", required = true, example = "8")
    private Long ingredientId;

    @Schema(description = "Название ингредиента (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "Говядина мраморная")
    private String ingredientName;

    @Schema(description = "Текущее количество на складе", required = true, example = "45.5")
    private BigDecimal quantity;

    @Schema(description = "Минимальное количество (порог для заказа)", example = "10.0")
    private BigDecimal minQuantity;

    @Schema(description = "Максимальное количество на складе", example = "100.0")
    private BigDecimal maxQuantity;

    @Schema(description = "Дата и время последнего обновления", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-12-11T14:30:00")
    private LocalDateTime lastUpdated;
}
