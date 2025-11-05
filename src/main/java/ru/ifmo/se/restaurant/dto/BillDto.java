package ru.ifmo.se.restaurant.dto;

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
public class BillDto {
    private Long id;

    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    private BigDecimal subtotal;

    private BigDecimal discount;

    private BigDecimal tax;

    private BigDecimal total;

    private LocalDateTime issuedAt;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}

