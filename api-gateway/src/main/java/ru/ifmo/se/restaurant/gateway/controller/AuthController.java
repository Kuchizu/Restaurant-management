package ru.ifmo.se.restaurant.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.gateway.dto.*;
import ru.ifmo.se.restaurant.gateway.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Аутентификация и авторизация")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/init")
    public Mono<UserDto> initAdmin() {
        return authService.initAdmin();
    }

    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя и получение токенов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный вход",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Обновление токена", description = "Получение нового access токена по refresh токену")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Недействительный refresh токен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public Mono<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @Operation(summary = "Выход из системы", description = "Инвалидация refresh токена")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Успешный выход")
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.logout(request.getRefreshToken());
    }

    @Operation(summary = "Получить текущего пользователя", description = "Возвращает данные авторизованного пользователя")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные пользователя",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public Mono<UserDto> getCurrentUser(ServerWebExchange exchange) {
        String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userIdHeader == null) {
            return Mono.error(new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Not authenticated"));
        }
        Long userId = Long.parseLong(userIdHeader);
        return authService.getCurrentUser(userId);
    }

    @Operation(summary = "Изменить пароль", description = "Изменение пароля текущего пользователя")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пароль успешно изменен"),
            @ApiResponse(responseCode = "400", description = "Неверный старый пароль",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> changePassword(ServerWebExchange exchange, @Valid @RequestBody ChangePasswordRequest request) {
        String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userIdHeader == null) {
            return Mono.error(new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Not authenticated"));
        }
        Long userId = Long.parseLong(userIdHeader);
        return authService.changePassword(userId, request);
    }

}
