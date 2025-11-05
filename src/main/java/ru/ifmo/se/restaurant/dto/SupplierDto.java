package ru.ifmo.se.restaurant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDto {
    private Long id;

    @NotBlank(message = "Supplier name cannot be blank")
    @Size(min = 1, max = 100, message = "Supplier name must be between 1 and 100 characters")
    private String name;

    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private Boolean isActive;
}

