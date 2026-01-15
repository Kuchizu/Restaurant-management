package ru.ifmo.se.restaurant.gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ifmo.se.restaurant.gateway.entity.User;
import ru.ifmo.se.restaurant.gateway.entity.UserRole;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "dGhpcyBpcyBhIHZlcnkgc2VjdXJlIGp3dCBzZWNyZXQga2V5IGZvciByZXN0YXVyYW50IG1hbmFnZW1lbnQ=");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test@restaurant.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(UserRole.ADMIN);
        testUser.setEmployeeId(5L);
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void generateAccessToken_ShouldReturnValidToken() {
        String token = jwtService.generateAccessToken(testUser);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void generateRefreshToken_ShouldReturnValidToken() {
        String token = jwtService.generateRefreshToken(testUser);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateAccessToken(testUser);

        String username = jwtService.extractUsername(token);

        assertEquals("test@restaurant.com", username);
    }

    @Test
    void extractRole_ShouldReturnCorrectRole() {
        String token = jwtService.generateAccessToken(testUser);

        UserRole role = jwtService.extractRole(token);

        assertEquals(UserRole.ADMIN, role);
    }

    @Test
    void extractUserId_ShouldReturnCorrectUserId() {
        String token = jwtService.generateAccessToken(testUser);

        Long userId = jwtService.extractUserId(token);

        assertEquals(1L, userId);
    }

    @Test
    void extractEmployeeId_ShouldReturnCorrectEmployeeId() {
        String token = jwtService.generateAccessToken(testUser);

        Long employeeId = jwtService.extractEmployeeId(token);

        assertEquals(5L, employeeId);
    }

    @Test
    void extractEmployeeId_WhenNull_ShouldReturnNull() {
        testUser.setEmployeeId(null);
        String token = jwtService.generateAccessToken(testUser);

        Long employeeId = jwtService.extractEmployeeId(token);

        assertNull(employeeId);
    }

    @Test
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        String token = jwtService.generateAccessToken(testUser);

        boolean isValid = jwtService.isTokenValid(token, "test@restaurant.com");

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithWrongUsername_ShouldReturnFalse() {
        String token = jwtService.generateAccessToken(testUser);

        boolean isValid = jwtService.isTokenValid(token, "wrong@restaurant.com");

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithoutUsername_ShouldReturnTrue() {
        String token = jwtService.generateAccessToken(testUser);

        boolean isValid = jwtService.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithInvalidToken_ShouldReturnFalse() {
        boolean isValid = jwtService.isTokenValid("invalid.token.here");

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1000L);
        String token = jwtService.generateAccessToken(testUser);

        boolean isValid = jwtService.isTokenValid(token);

        assertFalse(isValid);
    }

    @Test
    void getAccessTokenExpiration_ShouldReturnConfiguredValue() {
        long expiration = jwtService.getAccessTokenExpiration();

        assertEquals(900000L, expiration);
    }

    @Test
    void getRefreshTokenExpiration_ShouldReturnConfiguredValue() {
        long expiration = jwtService.getRefreshTokenExpiration();

        assertEquals(604800000L, expiration);
    }

    @Test
    void generateAccessToken_WithDifferentRoles_ShouldWork() {
        for (UserRole role : UserRole.values()) {
            testUser.setRole(role);
            String token = jwtService.generateAccessToken(testUser);

            assertEquals(role, jwtService.extractRole(token));
        }
    }
}
