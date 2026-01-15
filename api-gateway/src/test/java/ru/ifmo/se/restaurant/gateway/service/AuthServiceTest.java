package ru.ifmo.se.restaurant.gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.ifmo.se.restaurant.gateway.dto.*;
import ru.ifmo.se.restaurant.gateway.entity.RefreshToken;
import ru.ifmo.se.restaurant.gateway.entity.User;
import ru.ifmo.se.restaurant.gateway.entity.UserRole;
import ru.ifmo.se.restaurant.gateway.repository.RefreshTokenRepository;
import ru.ifmo.se.restaurant.gateway.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test@restaurant.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(UserRole.ADMIN);
        testUser.setEmployeeId(5L);
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());

        loginRequest = new LoginRequest();
        loginRequest.setUsername("test@restaurant.com");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("new@restaurant.com");
        registerRequest.setPassword("newpassword");
        registerRequest.setRole(UserRole.WAITER);
        registerRequest.setEmployeeId(10L);
    }

    @Test
    void initAdmin_WhenNoUsersExist_ShouldCreateAdmin() {
        when(userRepository.count()).thenReturn(Mono.just(0L));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

        StepVerifier.create(authService.initAdmin())
                .assertNext(userDto -> {
                    assertNotNull(userDto);
                    assertEquals("test@restaurant.com", userDto.getUsername());
                })
                .verifyComplete();
    }

    @Test
    void initAdmin_WhenUsersExist_ShouldThrowConflict() {
        when(userRepository.count()).thenReturn(Mono.just(1L));

        StepVerifier.create(authService.initAdmin())
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 409)
                .verify();
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(Mono.just(new RefreshToken()));

        StepVerifier.create(authService.login(loginRequest))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("accessToken", response.getAccessToken());
                    assertNotNull(response.getRefreshToken());
                    assertNotNull(response.getUser());
                })
                .verifyComplete();
    }

    @Test
    void login_WithInvalidUsername_ShouldThrowUnauthorized() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(authService.login(loginRequest))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 401)
                .verify();
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowUnauthorized() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        StepVerifier.create(authService.login(loginRequest))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 401)
                .verify();
    }

    @Test
    void login_WithDisabledAccount_ShouldThrowUnauthorized() {
        testUser.setEnabled(false);
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(testUser));

        StepVerifier.create(authService.login(loginRequest))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 401)
                .verify();
    }

    @Test
    void registerUser_WithNewUsername_ShouldCreateUser() {
        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("new@restaurant.com");
        newUser.setRole(UserRole.WAITER);
        newUser.setEnabled(true);
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(newUser));

        StepVerifier.create(authService.registerUser(registerRequest))
                .assertNext(userDto -> {
                    assertNotNull(userDto);
                    assertEquals("new@restaurant.com", userDto.getUsername());
                    assertEquals(UserRole.WAITER, userDto.getRole());
                })
                .verifyComplete();
    }

    @Test
    void registerUser_WithExistingUsername_ShouldThrowConflict() {
        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(authService.registerUser(registerRequest))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 409)
                .verify();
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2@restaurant.com");
        user2.setRole(UserRole.WAITER);
        user2.setEnabled(true);

        when(userRepository.findAll()).thenReturn(Flux.just(testUser, user2));

        StepVerifier.create(authService.getAllUsers())
                .assertNext(users -> {
                    assertNotNull(users);
                    assertEquals(2, users.size());
                })
                .verifyComplete();
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("validRefreshToken");
        refreshToken.setUserId(1L);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("validRefreshToken");

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Mono.just(refreshToken));
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(refreshTokenRepository.delete(any(RefreshToken.class))).thenReturn(Mono.empty());
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("newAccessToken");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(Mono.just(new RefreshToken()));

        StepVerifier.create(authService.refreshToken(request))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("newAccessToken", response.getAccessToken());
                })
                .verifyComplete();
    }

    @Test
    void refreshToken_WithExpiredToken_ShouldThrowUnauthorized() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("expiredToken");
        refreshToken.setUserId(1L);
        refreshToken.setExpiresAt(LocalDateTime.now().minusDays(1));

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expiredToken");

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Mono.just(refreshToken));
        when(refreshTokenRepository.delete(any(RefreshToken.class))).thenReturn(Mono.empty());

        StepVerifier.create(authService.refreshToken(request))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 401)
                .verify();
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowUnauthorized() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalidToken");

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(authService.refreshToken(request))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 401)
                .verify();
    }

    @Test
    void logout_ShouldDeleteRefreshToken() {
        when(refreshTokenRepository.deleteByToken(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(authService.logout("someToken"))
                .verifyComplete();

        verify(refreshTokenRepository).deleteByToken("someToken");
    }

    @Test
    void getCurrentUser_WhenUserExists_ShouldReturnUserDto() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));

        StepVerifier.create(authService.getCurrentUser(1L))
                .assertNext(userDto -> {
                    assertNotNull(userDto);
                    assertEquals("test@restaurant.com", userDto.getUsername());
                    assertEquals(UserRole.ADMIN, userDto.getRole());
                })
                .verifyComplete();
    }

    @Test
    void getCurrentUser_WhenUserNotExists_ShouldThrowNotFound() {
        when(userRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(authService.getCurrentUser(999L))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    @Test
    void changePassword_WithValidOldPassword_ShouldSucceed() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

        StepVerifier.create(authService.changePassword(1L, request))
                .verifyComplete();
    }

    @Test
    void changePassword_WithInvalidOldPassword_ShouldThrowBadRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        StepVerifier.create(authService.changePassword(1L, request))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 400)
                .verify();
    }

    @Test
    void changePassword_WhenUserNotFound_ShouldThrowNotFound() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(authService.changePassword(999L, request))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    @Test
    void refreshToken_WithDisabledUser_ShouldThrowUnauthorized() {
        testUser.setEnabled(false);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("validRefreshToken");
        refreshToken.setUserId(1L);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("validRefreshToken");

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Mono.just(refreshToken));
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));

        StepVerifier.create(authService.refreshToken(request))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 401)
                .verify();
    }
}
