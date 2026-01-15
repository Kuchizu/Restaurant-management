package ru.ifmo.se.restaurant.gateway.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AuthOpenApiController {

    private static final String AUTH_OPENAPI_SPEC = """
            {
              "openapi": "3.0.1",
              "info": {
                "title": "Auth API (Gateway)",
                "version": "1.0",
                "description": "## Как авторизоваться\\n\\n1. POST `/api/auth/init` - создать админа (только первый раз)\\n2. POST `/api/auth/login` - получить токен\\n3. Нажать **Authorize** и ввести токен\\n\\n**Логин:** admin@restaurant.com\\n**Пароль:** admin123\\n\\n## Роли\\n- **ADMIN** - полный доступ, может создавать пользователей\\n- **MANAGER** - управление рестораном, может создавать пользователей\\n- **CHEF** - повар, доступ к кухне\\n- **WAITER** - официант, доступ к заказам"
              },
              "servers": [{"url": "/"}],
              "security": [{"bearerAuth": []}],
              "components": {
                "securitySchemes": {
                  "bearerAuth": {
                    "type": "http",
                    "scheme": "bearer",
                    "bearerFormat": "JWT"
                  }
                },
                "schemas": {
                  "LoginRequest": {
                    "type": "object",
                    "required": ["username", "password"],
                    "properties": {
                      "username": {"type": "string", "example": "admin@restaurant.com"},
                      "password": {"type": "string", "example": "admin123"}
                    }
                  },
                  "LoginResponse": {
                    "type": "object",
                    "properties": {
                      "accessToken": {"type": "string"},
                      "refreshToken": {"type": "string"},
                      "user": {"$ref": "#/components/schemas/UserDto"}
                    }
                  },
                  "UserDto": {
                    "type": "object",
                    "properties": {
                      "id": {"type": "integer", "format": "int64"},
                      "username": {"type": "string"},
                      "role": {"type": "string", "enum": ["WAITER", "CHEF", "MANAGER", "ADMIN"]},
                      "employeeId": {"type": "integer", "format": "int64"},
                      "enabled": {"type": "boolean"},
                      "createdAt": {"type": "string", "format": "date-time"},
                      "lastLogin": {"type": "string", "format": "date-time"}
                    }
                  },
                  "RegisterRequest": {
                    "type": "object",
                    "required": ["username", "password", "role"],
                    "properties": {
                      "username": {"type": "string", "example": "waiter@restaurant.com"},
                      "password": {"type": "string", "minLength": 6, "example": "password123"},
                      "role": {"type": "string", "enum": ["WAITER", "CHEF", "MANAGER", "ADMIN"]},
                      "employeeId": {"type": "integer", "format": "int64"}
                    }
                  },
                  "RefreshTokenRequest": {
                    "type": "object",
                    "required": ["refreshToken"],
                    "properties": {
                      "refreshToken": {"type": "string"}
                    }
                  },
                  "ChangePasswordRequest": {
                    "type": "object",
                    "required": ["oldPassword", "newPassword"],
                    "properties": {
                      "oldPassword": {"type": "string"},
                      "newPassword": {"type": "string", "minLength": 6}
                    }
                  },
                  "ErrorResponse": {
                    "type": "object",
                    "properties": {
                      "timestamp": {"type": "string", "format": "date-time"},
                      "status": {"type": "integer"},
                      "error": {"type": "string"},
                      "message": {"type": "string"},
                      "path": {"type": "string"}
                    }
                  }
                }
              },
              "paths": {
                "/api/auth/init": {
                  "post": {
                    "tags": ["Authentication"],
                    "summary": "Инициализация админа",
                    "description": "Создает администратора при первом запуске. Можно вызвать только один раз.",
                    "responses": {
                      "200": {"description": "Админ создан", "content": {"application/json": {"schema": {"$ref": "#/components/schemas/UserDto"}}}},
                      "409": {"description": "Админ уже существует"}
                    }
                  }
                },
                "/api/auth/login": {
                  "post": {
                    "tags": ["Authentication"],
                    "summary": "Вход в систему",
                    "description": "Аутентификация пользователя и получение JWT токенов",
                    "requestBody": {
                      "required": true,
                      "content": {"application/json": {"schema": {"$ref": "#/components/schemas/LoginRequest"}}}
                    },
                    "responses": {
                      "200": {"description": "Успешный вход", "content": {"application/json": {"schema": {"$ref": "#/components/schemas/LoginResponse"}}}},
                      "401": {"description": "Неверные учетные данные"}
                    }
                  }
                },
                "/api/auth/users": {
                  "get": {
                    "tags": ["User Management"],
                    "summary": "Список пользователей",
                    "description": "Получить всех пользователей. **Только для ADMIN и MANAGER!**",
                    "security": [{"bearerAuth": []}],
                    "responses": {
                      "200": {"description": "Список пользователей", "content": {"application/json": {"schema": {"type": "array", "items": {"$ref": "#/components/schemas/UserDto"}}}}},
                      "403": {"description": "Нет прав"}
                    }
                  },
                  "post": {
                    "tags": ["User Management"],
                    "summary": "Создать пользователя",
                    "description": "Создание нового пользователя. **Только для ADMIN и MANAGER (супервайзеры)!**",
                    "security": [{"bearerAuth": []}],
                    "requestBody": {
                      "required": true,
                      "content": {"application/json": {"schema": {"$ref": "#/components/schemas/RegisterRequest"}}}
                    },
                    "responses": {
                      "201": {"description": "Пользователь создан", "content": {"application/json": {"schema": {"$ref": "#/components/schemas/UserDto"}}}},
                      "403": {"description": "Нет прав (требуется ADMIN или MANAGER)"},
                      "409": {"description": "Пользователь уже существует"}
                    }
                  }
                },
                "/api/auth/me": {
                  "get": {
                    "tags": ["Authentication"],
                    "summary": "Текущий пользователь",
                    "description": "Получить данные авторизованного пользователя",
                    "security": [{"bearerAuth": []}],
                    "responses": {
                      "200": {"description": "Данные пользователя", "content": {"application/json": {"schema": {"$ref": "#/components/schemas/UserDto"}}}},
                      "401": {"description": "Не авторизован"}
                    }
                  }
                },
                "/api/auth/refresh": {
                  "post": {
                    "tags": ["Authentication"],
                    "summary": "Обновить токен",
                    "description": "Получение нового access токена по refresh токену",
                    "requestBody": {
                      "required": true,
                      "content": {"application/json": {"schema": {"$ref": "#/components/schemas/RefreshTokenRequest"}}}
                    },
                    "responses": {
                      "200": {"description": "Токен обновлен", "content": {"application/json": {"schema": {"$ref": "#/components/schemas/LoginResponse"}}}},
                      "401": {"description": "Недействительный refresh токен"}
                    }
                  }
                },
                "/api/auth/logout": {
                  "post": {
                    "tags": ["Authentication"],
                    "summary": "Выход из системы",
                    "description": "Инвалидация refresh токена",
                    "security": [{"bearerAuth": []}],
                    "requestBody": {
                      "required": true,
                      "content": {"application/json": {"schema": {"$ref": "#/components/schemas/RefreshTokenRequest"}}}
                    },
                    "responses": {
                      "204": {"description": "Успешный выход"}
                    }
                  }
                },
                "/api/auth/change-password": {
                  "post": {
                    "tags": ["Authentication"],
                    "summary": "Изменить пароль",
                    "description": "Изменение пароля текущего пользователя",
                    "security": [{"bearerAuth": []}],
                    "requestBody": {
                      "required": true,
                      "content": {"application/json": {"schema": {"$ref": "#/components/schemas/ChangePasswordRequest"}}}
                    },
                    "responses": {
                      "204": {"description": "Пароль изменен"},
                      "400": {"description": "Неверный старый пароль"},
                      "401": {"description": "Не авторизован"}
                    }
                  }
                }
              }
            }
            """;

    @GetMapping(value = "/auth-api/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getAuthApiDocs() {
        return Mono.just(AUTH_OPENAPI_SPEC);
    }
}
