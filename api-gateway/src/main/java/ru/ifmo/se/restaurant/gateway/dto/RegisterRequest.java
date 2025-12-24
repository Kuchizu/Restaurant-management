package ru.ifmo.se.restaurant.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.gateway.entity.UserRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на регистрацию нового пользователя")
public class RegisterRequest {
    @NotBlank(message = "Username cannot be blank")
    @Schema(description = "Имя пользователя", required = true, example = "waiter@restaurant.com")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "Пароль", required = true, example = "password123")
    private String password;

    @NotNull(message = "Role cannot be null")
    @Schema(description = "Роль пользователя", required = true, example = "WAITER")
    private UserRole role;

    @Schema(description = "ID связанного сотрудника", example = "5")
    private Long employeeId;
}
