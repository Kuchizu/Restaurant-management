package ru.ifmo.se.restaurant.inventory.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ об ошибке")
public class ErrorResponse {
    @Schema(description = "Временная метка", example = "2025-12-11T14:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP статус код", example = "404")
    private int status;

    @Schema(description = "Тип ошибки", example = "Not Found")
    private String error;

    @Schema(description = "Сообщение об ошибке", example = "Resource not found")
    private String message;

    @Schema(description = "Путь запроса", example = "/api/inventory/999")
    private String path;

    @Schema(description = "Дополнительные детали ошибки")
    private Map<String, Object> details;
}
