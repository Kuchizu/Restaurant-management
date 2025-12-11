package ru.ifmo.se.restaurant.order.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("tables")
public class RestaurantTable {
    @Id
    private Long id;
    private String tableNumber;
    private Integer capacity;
    private String location;
    private TableStatus status;
}
