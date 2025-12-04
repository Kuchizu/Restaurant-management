package ru.ifmo.se.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.model.EmployeeRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employee data transfer object")
public class EmployeeDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Employee unique identifier", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Schema(description = "Employee first name", requiredMode = Schema.RequiredMode.REQUIRED, example = "John")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Schema(description = "Employee last name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Smith")
    private String lastName;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Schema(description = "Employee email address", example = "john.smith@restaurant.com")
    private String email;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    @Schema(description = "Employee phone number", example = "+1234567890")
    private String phone;

    @NotNull(message = "Role cannot be null")
    @Schema(description = "Employee role", requiredMode = Schema.RequiredMode.REQUIRED, example = "WAITER")
    private EmployeeRole role;

    @Schema(description = "Whether employee is active", example = "true")
    private Boolean isActive;
}
