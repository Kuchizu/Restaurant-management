package ru.ifmo.se.restaurant.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на смену пароля")
public class ChangePasswordRequest {
    @NotBlank(message = "Old password cannot be blank")
    @Schema(description = "Текущий пароль", required = true)
    private String oldPassword;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "Новый пароль", required = true)
    private String newPassword;
}
