package ru.ifmo.se.restaurant.kitchen.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Элемент кухонной очереди")
public class KitchenQueueDto {
    @Schema(description = "ID элемента очереди (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotNull(message = "Order ID cannot be null")
    @Schema(description = "ID заказа", required = true, example = "5")
    private Long orderId;

    @NotNull(message = "Order item ID cannot be null")
    @Schema(description = "ID позиции заказа", required = true, example = "12")
    private Long orderItemId;

    @Schema(description = "Название блюда", example = "Стейк Рибай медиум", required = true)
    private String dishName;

    @Schema(description = "Количество порций", example = "1", required = true)
    private Integer quantity;

    @Schema(description = "Статус приготовления блюда", accessMode = Schema.AccessMode.READ_ONLY, example = "PENDING")
    private DishStatus status;

    @Schema(description = "Особые пожелания к приготовлению", example = "Средней прожарки, без перца")
    private String specialRequest;

    @Schema(description = "Дата и время добавления в очередь", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-12-11T15:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Дата и время начала приготовления", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-12-11T15:35:00")
    private LocalDateTime startedAt;

    @Schema(description = "Дата и время завершения приготовления", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-12-11T15:50:00")
    private LocalDateTime completedAt;

    public static KitchenQueueDto fromDomain(KitchenQueue queue) {
        return new KitchenQueueDto(
            queue.getId(),
            queue.getOrderId(),
            queue.getOrderItemId(),
            queue.getDishName(),
            queue.getQuantity(),
            queue.getStatus(),
            queue.getSpecialRequest(),
            queue.getCreatedAt(),
            queue.getStartedAt(),
            queue.getCompletedAt()
        );
    }
}
