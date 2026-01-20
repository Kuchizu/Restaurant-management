package ru.ifmo.se.restaurant.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на добавление блюда в кухонную очередь")
public class KitchenQueueRequest {
    @NotNull(message = "Order ID cannot be null")
    @Schema(description = "ID заказа", required = true, example = "15")
    private Long orderId;

    @NotNull(message = "Order item ID cannot be null")
    @Schema(description = "ID позиции заказа", required = true, example = "42")
    private Long orderItemId;

    @NotBlank(message = "Dish name cannot be blank")
    @Schema(description = "Название блюда", required = true, example = "Стейк Рибай медиум")
    private String dishName;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Количество порций", required = true, example = "2")
    private Integer quantity;

    @Schema(description = "Особые пожелания к приготовлению", example = "Средней прожарки, без перца")
    private String specialRequest;
}
