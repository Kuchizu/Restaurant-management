package ru.ifmo.se.restaurant.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.gateway.entity.UserRole;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные пользователя")
public class UserDto {
    @Schema(description = "ID пользователя", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @Schema(description = "Имя пользователя", example = "admin@restaurant.com")
    private String username;

    @Schema(description = "Роль пользователя", example = "ADMIN")
    private UserRole role;

    @Schema(description = "ID связанного сотрудника", example = "5")
    private Long employeeId;

    @Schema(description = "Активен ли пользователь", example = "true")
    private Boolean enabled;

    @Schema(description = "Дата создания", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Дата последнего входа", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastLogin;
}
