package ru.ifmo.se.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bill data transfer object")
public class BillDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Bill unique identifier", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotNull(message = "Order ID cannot be null")
    @Schema(description = "Order ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long orderId;

    @Schema(description = "Subtotal amount", accessMode = Schema.AccessMode.READ_ONLY, example = "42.99")
    private BigDecimal subtotal;

    @Schema(description = "Discount amount", example = "5.00")
    private BigDecimal discount;

    @Schema(description = "Tax amount", accessMode = Schema.AccessMode.READ_ONLY, example = "3.80")
    private BigDecimal tax;

    @Schema(description = "Total bill amount", accessMode = Schema.AccessMode.READ_ONLY, example = "41.79")
    private BigDecimal total;

    @Schema(description = "Bill issued timestamp", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-12-05T11:30:00")
    private LocalDateTime issuedAt;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Additional notes", example = "Thank you for dining with us")
    private String notes;
}
