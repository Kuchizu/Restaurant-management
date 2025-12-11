package ru.ifmo.se.restaurant.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные заказа")
public class OrderDto {
    @Schema(description = "ID заказа (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotNull(message = "Table ID cannot be null")
    @Schema(description = "ID стола", required = true, example = "5")
    private Long tableId;

    @NotNull(message = "Waiter ID cannot be null")
    @Schema(description = "ID официанта", required = true, example = "2")
    private Long waiterId;

    @Schema(description = "Статус заказа", accessMode = Schema.AccessMode.READ_ONLY, example = "PENDING")
    private OrderStatus status;

    @Schema(description = "Общая сумма заказа", accessMode = Schema.AccessMode.READ_ONLY, example = "1250.00")
    private BigDecimal totalAmount;

    @Schema(description = "Особые пожелания к заказу", example = "Без лука, пожалуйста")
    private String specialRequests;

    @Schema(description = "Дата и время создания заказа", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-12-11T15:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Дата и время закрытия заказа", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-12-11T16:45:00")
    private LocalDateTime closedAt;

    @Valid
    @Schema(description = "Список блюд в заказе")
    private List<OrderItemDto> items;
}
