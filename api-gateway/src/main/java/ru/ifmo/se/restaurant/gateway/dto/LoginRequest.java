package ru.ifmo.se.restaurant.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на вход в систему")
public class LoginRequest {
    @NotBlank(message = "Username cannot be blank")
    @Schema(description = "Имя пользователя", required = true, example = "admin@restaurant.com")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Schema(description = "Пароль", required = true, example = "admin123")
    private String password;
}
