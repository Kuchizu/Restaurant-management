package ru.ifmo.se.restaurant.dto;

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
public class SupplyOrderDto {
    private Long id;

    @NotNull(message = "Supplier ID cannot be null")
    private Long supplierId;

    private String supplierName;

    private SupplyOrderStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime orderedAt;

    private LocalDateTime receivedAt;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private List<SupplyOrderIngredientDto> ingredients;
}

