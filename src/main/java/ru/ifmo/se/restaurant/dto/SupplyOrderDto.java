package ru.ifmo.se.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.model.SupplyOrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SupplyOrder data transfer object")
public class SupplyOrderDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Supply order unique identifier", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotNull(message = "Supplier ID cannot be null")
    @Schema(description = "Supplier identifier", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long supplierId;

    @Schema(description = "Supplier name", example = "Fresh Foods Inc.")
    private String supplierName;

    @Schema(description = "Supply order status", example = "PENDING")
    private SupplyOrderStatus status;

    @Schema(description = "Creation timestamp", example = "2025-12-05T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Order placement timestamp", example = "2025-12-05T10:30:00")
    private LocalDateTime orderedAt;

    @Schema(description = "Receipt timestamp", example = "2025-12-06T09:00:00")
    private LocalDateTime receivedAt;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Schema(description = "Additional notes", example = "Urgent delivery needed")
    private String notes;

    @Schema(description = "List of ingredients in order")
    private List<SupplyOrderIngredientDto> ingredients;
}
