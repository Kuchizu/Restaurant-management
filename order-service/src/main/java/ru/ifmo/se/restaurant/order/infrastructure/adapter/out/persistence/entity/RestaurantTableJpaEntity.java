package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.order.domain.valueobject.TableStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("tables")
public class RestaurantTableJpaEntity {
    @Id
    private Long id;
    private String tableNumber;
    private Integer capacity;
    private String location;
    private TableStatus status;
}
