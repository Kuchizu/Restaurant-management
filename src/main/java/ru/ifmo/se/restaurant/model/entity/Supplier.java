package ru.ifmo.se.restaurant.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Supplier name cannot be blank")
    @Size(min = 1, max = 100, message = "Supplier name must be between 1 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 200, message = "Address cannot exceed 200 characters")
    @Column(length = 200)
    private String address;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Column(length = 100)
    private String email;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    @Column(length = 20)
    private String phone;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplyOrder> supplyOrders = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isActive = true;
}

