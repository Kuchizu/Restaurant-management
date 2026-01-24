package ru.ifmo.se.restaurant.order.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.order.domain.valueobject.TableStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTable {
    private Long id;
    private String tableNumber;
    private Integer capacity;
    private String location;
    private TableStatus status;
}
