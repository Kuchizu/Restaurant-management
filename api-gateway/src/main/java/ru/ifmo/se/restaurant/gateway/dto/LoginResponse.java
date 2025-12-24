package ru.ifmo.se.restaurant.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ на успешный вход")
public class LoginResponse {
    @Schema(description = "Access токен (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh токен для обновления access токена")
    private String refreshToken;

    @Schema(description = "Данные пользователя")
    private UserDto user;
}
