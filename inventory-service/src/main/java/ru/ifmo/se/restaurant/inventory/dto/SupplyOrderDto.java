package ru.ifmo.se.restaurant.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.inventory.entity.SupplyOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Заказ поставки у поставщика")
public class SupplyOrderDto {
    @Schema(description = "ID заказа (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "15")
    private Long id;

    @NotNull(message = "Supplier ID is required")
    @Schema(description = "ID поставщика", required = true, example = "3")
    private Long supplierId;

    @Schema(description = "Название поставщика (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "ООО 'Мясная лавка'")
    private String supplierName;

    @Schema(description = "Дата и время создания заказа", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-12-11T10:00:00")
    private LocalDateTime orderDate;

    @Schema(description = "Планируемая дата доставки", example = "2025-12-13T14:00:00")
    private LocalDateTime deliveryDate;

    @Schema(description = "Статус заказа", accessMode = Schema.AccessMode.READ_ONLY, example = "PENDING")
    private SupplyOrderStatus status;

    @Schema(description = "Общая стоимость заказа (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "48500.00")
    private BigDecimal totalCost;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Schema(description = "Примечания к заказу", example = "Доставка через задний вход, с 14:00 до 16:00")
    private String notes;

    @Valid
    @Schema(description = "Список позиций заказа")
    private List<SupplyOrderItemDto> items;
}
