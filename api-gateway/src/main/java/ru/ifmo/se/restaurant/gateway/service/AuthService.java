package ru.ifmo.se.restaurant.gateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.gateway.dto.*;
import ru.ifmo.se.restaurant.gateway.entity.RefreshToken;
import ru.ifmo.se.restaurant.gateway.entity.User;
import ru.ifmo.se.restaurant.gateway.repository.RefreshTokenRepository;
import ru.ifmo.se.restaurant.gateway.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public Mono<UserDto> initAdmin() {
        return userRepository.count()
                .flatMap(count -> {
                    if (count > 0) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Admin already exists"));
                    }
                    User admin = new User();
                    admin.setUsername("admin@restaurant.com");
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRole(ru.ifmo.se.restaurant.gateway.entity.UserRole.ADMIN);
                    admin.setEnabled(true);
                    admin.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(admin);
                })
                .map(this::toUserDto);
    }

    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .flatMap(user -> {
                    if (!user.getEnabled()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is disabled"));
                    }
                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
                    }
                    return createLoginResponse(user);
                });
    }

    public Mono<UserDto> registerUser(RegisterRequest request) {
        return userRepository.existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"));
                    }
                    User user = new User();
                    user.setUsername(request.getUsername());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setRole(request.getRole());
                    user.setEmployeeId(request.getEmployeeId());
                    user.setEnabled(true);
                    user.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .map(this::toUserDto);
    }

    public Mono<java.util.List<UserDto>> getAllUsers() {
        return userRepository.findAll()
                .map(this::toUserDto)
                .collectList();
    }

    public Mono<LoginResponse> refreshToken(RefreshTokenRequest request) {
        return refreshTokenRepository.findByToken(request.getRefreshToken())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")))
                .flatMap(refreshToken -> {
                    if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return refreshTokenRepository.delete(refreshToken)
                                .then(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired")));
                    }
                    return userRepository.findById(refreshToken.getUserId())
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")))
                            .flatMap(user -> {
                                if (!user.getEnabled()) {
                                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is disabled"));
                                }
                                return refreshTokenRepository.delete(refreshToken)
                                        .then(createLoginResponse(user));
                            });
                });
    }

    public Mono<Void> logout(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")))
                .flatMap(token -> refreshTokenRepository.delete(token));
    }

    public Mono<UserDto> getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .map(this::toUserDto);
    }

    public Mono<Void> changePassword(Long userId, ChangePasswordRequest request) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid old password"));
                    }
                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    return userRepository.save(user);
                })
                .then();
    }

    private Mono<LoginResponse> createLoginResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUserId(user.getId());
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000));
        refreshToken.setCreatedAt(LocalDateTime.now());

        user.setLastLogin(LocalDateTime.now());

        return userRepository.save(user)
                .then(refreshTokenRepository.save(refreshToken))
                .map(saved -> new LoginResponse(accessToken, refreshTokenValue, toUserDto(user)));
    }

    private UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getEmployeeId(),
                user.getEnabled(),
                user.getCreatedAt(),
                user.getLastLogin()
        );
    }
}
