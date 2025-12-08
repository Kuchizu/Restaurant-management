package ru.ifmo.se.restaurant.order.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("employees")
public class Employee {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private EmployeeRole role;
}
