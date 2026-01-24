package ru.ifmo.se.restaurant.inventory.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.inventory.domain.entity.Inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запись о складских запасах ингредиента")
public class InventoryDto {
    @Schema(description = "ID записи (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotNull(message = "Ingredient ID is required")
    @Schema(description = "ID ингредиента", required = true, example = "8")
    private Long ingredientId;

    @Schema(description = "Название ингредиента (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "Говядина мраморная")
    private String ingredientName;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", message = "Quantity must be non-negative")
    @Schema(description = "Текущее количество на складе", required = true, example = "45.5")
    private BigDecimal quantity;

    @DecimalMin(value = "0.0", message = "Minimum quantity must be non-negative")
    @Schema(description = "Минимальное количество (порог для заказа)", example = "10.0")
    private BigDecimal minQuantity;

    @DecimalMin(value = "0.0", message = "Maximum quantity must be non-negative")
    @Schema(description = "Максимальное количество на складе", example = "100.0")
    private BigDecimal maxQuantity;

    @Schema(description = "Дата и время последнего обновления", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-12-11T14:30:00")
    private LocalDateTime lastUpdated;

    public static InventoryDto fromDomain(Inventory inventory) {
        return InventoryDto.builder()
            .id(inventory.getId())
            .ingredientId(inventory.getIngredient().getId())
            .ingredientName(inventory.getIngredient().getName())
            .quantity(inventory.getQuantity())
            .minQuantity(inventory.getMinQuantity())
            .maxQuantity(inventory.getMaxQuantity())
            .lastUpdated(inventory.getLastUpdated())
            .build();
    }
}
