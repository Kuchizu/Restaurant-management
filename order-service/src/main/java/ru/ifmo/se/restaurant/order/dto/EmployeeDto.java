package ru.ifmo.se.restaurant.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.order.entity.EmployeeRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные сотрудника")
public class EmployeeDto {
    @Schema(description = "ID сотрудника (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "2")
    private Long id;

    @NotBlank(message = "First name cannot be blank")
    @Schema(description = "Имя сотрудника", required = true, example = "Иван")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Schema(description = "Фамилия сотрудника", required = true, example = "Петров")
    private String lastName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email cannot be blank")
    @Schema(description = "Email сотрудника", required = true, example = "ivan.petrov@restaurant.ru")
    private String email;

    @Schema(description = "Телефон сотрудника", example = "+7 (916) 555-12-34")
    private String phone;

    @NotNull(message = "Role cannot be null")
    @Schema(description = "Роль сотрудника", required = true, example = "WAITER")
    private EmployeeRole role;
}
