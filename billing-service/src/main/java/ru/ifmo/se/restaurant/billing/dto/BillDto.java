package ru.ifmo.se.restaurant.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.billing.entity.BillStatus;
import ru.ifmo.se.restaurant.billing.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные счета")
public class BillDto {
    @Schema(description = "ID счета (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "25")
    private Long id;

    @NotNull(message = "Order ID is required")
    @Schema(description = "ID заказа", required = true, example = "15")
    private Long orderId;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    @Schema(description = "Общая сумма заказа (без налогов и чаевых)", required = true, example = "2450.00")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    @Schema(description = "Сумма налога (НДС)", example = "441.00")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.0", message = "Service charge must be non-negative")
    @Schema(description = "Сумма за обслуживание (чаевые)", example = "245.00")
    private BigDecimal serviceCharge;

    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    @Schema(description = "Сумма скидки", example = "0.00")
    private BigDecimal discountAmount;

    @NotNull(message = "Final amount is required")
    @DecimalMin(value = "0.0", message = "Final amount must be non-negative")
    @Schema(description = "Итоговая сумма к оплате", required = true, example = "3136.00")
    private BigDecimal finalAmount;

    @NotNull(message = "Status is required")
    @Schema(description = "Статус счета", required = true, example = "PENDING")
    private BillStatus status;

    @Schema(description = "Способ оплаты", example = "CREDIT_CARD")
    private PaymentMethod paymentMethod;

    @Schema(description = "Дата и время создания счета", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-12-11T16:45:00")
    private LocalDateTime createdAt;

    @Schema(description = "Дата и время оплаты", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-12-11T17:00:00")
    private LocalDateTime paidAt;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Примечания к счету", example = "Применена скидка 10% по акции")
    private String notes;
}
