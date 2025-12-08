package ru.ifmo.se.restaurant.inventory.dto;

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
public class SupplyOrderDto {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private SupplyOrderStatus status;
    private BigDecimal totalCost;
    private String notes;
    private List<SupplyOrderItemDto> items;
}
