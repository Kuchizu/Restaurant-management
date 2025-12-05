package ru.ifmo.se.restaurant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Popular dishes report")
public class PopularDishesReportDto {
    @Schema(description = "Report start date", example = "2025-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Report end date", example = "2025-12-31T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "List of popular dishes")
    private List<DishPopularityDto> popularDishes;
}
