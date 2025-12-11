package ru.ifmo.se.restaurant.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.order.entity.TableStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableDto {
    private Long id;

    @NotBlank(message = "Table number cannot be blank")
    private String tableNumber;

    @NotNull(message = "Capacity cannot be null")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private String location;
    private TableStatus status;
}
