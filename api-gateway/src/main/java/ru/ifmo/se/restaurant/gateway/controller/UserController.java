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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.gateway.dto.ErrorResponse;
import ru.ifmo.se.restaurant.gateway.dto.RegisterRequest;
import ru.ifmo.se.restaurant.gateway.dto.UserDto;
import ru.ifmo.se.restaurant.gateway.service.UserService;

@RestController
@RequestMapping("/api/auth/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Управление пользователями (только для ADMIN и MANAGER)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    private Mono<Void> checkSupervisorRole(ServerWebExchange exchange) {
        String role = exchange.getRequest().getHeaders().getFirst("X-User-Role");
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied. Only supervisors (ADMIN, MANAGER) can manage users."));
        }
        return Mono.empty();
    }

    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public Flux<UserDto> getAllUsers(ServerWebExchange exchange) {
        return checkSupervisorRole(exchange).thenMany(userService.getAllUsers());
    }

    @Operation(summary = "Получить пользователей с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Страница пользователей"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @GetMapping("/paged")
    public Mono<ResponseEntity<Page<UserDto>>> getUsersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            ServerWebExchange exchange) {
        return checkSupervisorRole(exchange)
                .then(userService.getAllUsersPaginated(page, size))
                .map(pagedUsers -> {
                    exchange.getResponse().getHeaders().add("X-Total-Count", String.valueOf(pagedUsers.getTotalElements()));
                    exchange.getResponse().getHeaders().add("X-Total-Pages", String.valueOf(pagedUsers.getTotalPages()));
                    return ResponseEntity.ok(pagedUsers);
                });
    }

    @Operation(summary = "Получить пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные пользователя",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @GetMapping("/{id}")
    public Mono<UserDto> getUserById(@PathVariable Long id, ServerWebExchange exchange) {
        return checkSupervisorRole(exchange).then(userService.getUserById(id));
    }

    @Operation(summary = "Создать пользователя", description = "Создание нового пользователя в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь создан",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь уже существует"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserDto> createUser(@Valid @RequestBody RegisterRequest request, ServerWebExchange exchange) {
        return checkSupervisorRole(exchange).then(userService.createUser(request));
    }

    @Operation(summary = "Обновить пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь обновлен",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PutMapping("/{id}")
    public Mono<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto dto, ServerWebExchange exchange) {
        return checkSupervisorRole(exchange).then(userService.updateUser(id, dto));
    }

    @Operation(summary = "Удалить пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteUser(@PathVariable Long id, ServerWebExchange exchange) {
        return checkSupervisorRole(exchange).then(userService.deleteUser(id));
    }

    @Operation(summary = "Активировать пользователя")
    @PatchMapping("/{id}/enable")
    public Mono<UserDto> enableUser(@PathVariable Long id, ServerWebExchange exchange) {
        return checkSupervisorRole(exchange).then(userService.toggleUserEnabled(id, true));
    }

    @Operation(summary = "Деактивировать пользователя")
    @PatchMapping("/{id}/disable")
    public Mono<UserDto> disableUser(@PathVariable Long id, ServerWebExchange exchange) {
        return checkSupervisorRole(exchange).then(userService.toggleUserEnabled(id, false));
    }
}
