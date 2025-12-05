package ru.ifmo.se.restaurant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profitability report")
public class ProfitabilityReportDto {
    @Schema(description = "Report start date", example = "2025-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Report end date", example = "2025-12-31T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "Total revenue", example = "125000.00")
    private BigDecimal revenue;

    @Schema(description = "Total cost", example = "75000.00")
    private BigDecimal totalCost;

    @Schema(description = "Total profit", example = "50000.00")
    private BigDecimal profit;

    @Schema(description = "Profit margin percentage", example = "40.00")
    private BigDecimal profitMargin;
}
