package ru.ifmo.se.restaurant.inventory.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.inventory.domain.entity.SupplyOrderItem;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Позиция заказа поставки")
public class SupplyOrderItemDto {
    @Schema(description = "ID позиции (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "42")
    private Long id;

    @NotNull(message = "Ingredient ID is required")
    @Schema(description = "ID ингредиента", required = true, example = "8")
    private Long ingredientId;

    @Schema(description = "Название ингредиента (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "Говядина мраморная")
    private String ingredientName;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    @Schema(description = "Количество для заказа", required = true, example = "50.0")
    private BigDecimal quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @Schema(description = "Цена за единицу", required = true, example = "950.00")
    private BigDecimal unitPrice;

    public static SupplyOrderItemDto fromDomain(SupplyOrderItem item) {
        return SupplyOrderItemDto.builder()
            .id(item.getId())
            .ingredientId(item.getIngredient().getId())
            .ingredientName(item.getIngredient().getName())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .build();
    }
}
