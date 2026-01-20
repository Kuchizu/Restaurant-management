package ru.ifmo.se.restaurant.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.order.domain.valueobject.TableStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные стола")
public class TableDto {
    @Schema(description = "ID стола (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "5")
    private Long id;

    @NotBlank(message = "Table number cannot be blank")
    @Schema(description = "Номер стола", required = true, example = "A-12")
    private String tableNumber;

    @NotNull(message = "Capacity cannot be null")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Schema(description = "Вместимость (количество мест)", required = true, example = "4")
    private Integer capacity;

    @Schema(description = "Расположение стола в зале", example = "Основной зал, у окна")
    private String location;

    @Schema(description = "Статус стола", example = "FREE")
    private TableStatus status;
}
