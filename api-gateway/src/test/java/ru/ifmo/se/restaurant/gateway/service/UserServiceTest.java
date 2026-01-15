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
import ru.ifmo.se.restaurant.gateway.dto.RegisterRequest;
import ru.ifmo.se.restaurant.gateway.dto.UserDto;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

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
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2@restaurant.com");
        user2.setRole(UserRole.WAITER);
        user2.setEnabled(true);

        when(userRepository.findAll()).thenReturn(Flux.just(testUser, user2));

        StepVerifier.create(userService.getAllUsers())
                .assertNext(dto -> assertEquals("test@restaurant.com", dto.getUsername()))
                .assertNext(dto -> assertEquals("user2@restaurant.com", dto.getUsername()))
                .verifyComplete();
    }

    @Test
    void getAllUsersPaginated_ShouldReturnPagedUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2@restaurant.com");
        user2.setRole(UserRole.WAITER);
        user2.setEnabled(true);

        when(userRepository.findAll()).thenReturn(Flux.just(testUser, user2));

        StepVerifier.create(userService.getAllUsersPaginated(0, 10))
                .assertNext(page -> {
                    assertEquals(2, page.getTotalElements());
                    assertEquals(2, page.getContent().size());
                })
                .verifyComplete();
    }

    @Test
    void getAllUsersPaginated_WithPaging_ShouldReturnCorrectPage() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2@restaurant.com");
        user2.setRole(UserRole.WAITER);
        user2.setEnabled(true);

        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("user3@restaurant.com");
        user3.setRole(UserRole.CHEF);
        user3.setEnabled(true);

        when(userRepository.findAll()).thenReturn(Flux.just(testUser, user2, user3));

        StepVerifier.create(userService.getAllUsersPaginated(0, 2))
                .assertNext(page -> {
                    assertEquals(3, page.getTotalElements());
                    assertEquals(2, page.getContent().size());
                })
                .verifyComplete();
    }

    @Test
    void getUserById_WhenExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.getUserById(1L))
                .assertNext(dto -> {
                    assertEquals(1L, dto.getId());
                    assertEquals("test@restaurant.com", dto.getUsername());
                    assertEquals(UserRole.ADMIN, dto.getRole());
                })
                .verifyComplete();
    }

    @Test
    void getUserById_WhenNotExists_ShouldThrowNotFound() {
        when(userRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.getUserById(999L))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    @Test
    void createUser_WithNewUsername_ShouldCreateUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("new@restaurant.com");
        request.setPassword("password123");
        request.setRole(UserRole.WAITER);
        request.setEmployeeId(10L);

        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("new@restaurant.com");
        newUser.setRole(UserRole.WAITER);
        newUser.setEnabled(true);

        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(newUser));

        StepVerifier.create(userService.createUser(request))
                .assertNext(dto -> {
                    assertEquals("new@restaurant.com", dto.getUsername());
                    assertEquals(UserRole.WAITER, dto.getRole());
                })
                .verifyComplete();
    }

    @Test
    void createUser_WithExistingUsername_ShouldThrowConflict() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing@restaurant.com");
        request.setPassword("password123");
        request.setRole(UserRole.WAITER);

        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(userService.createUser(request))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 409)
                .verify();
    }

    @Test
    void updateUser_WhenExists_ShouldUpdateRole() {
        UserDto updateDto = new UserDto();
        updateDto.setRole(UserRole.MANAGER);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("test@restaurant.com");
        updatedUser.setRole(UserRole.MANAGER);
        updatedUser.setEnabled(true);

        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));

        StepVerifier.create(userService.updateUser(1L, updateDto))
                .assertNext(dto -> assertEquals(UserRole.MANAGER, dto.getRole()))
                .verifyComplete();
    }

    @Test
    void updateUser_ShouldUpdateEmployeeId() {
        UserDto updateDto = new UserDto();
        updateDto.setEmployeeId(20L);

        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(userService.updateUser(1L, updateDto))
                .assertNext(dto -> assertNotNull(dto))
                .verifyComplete();

        verify(userRepository).save(argThat(user -> user.getEmployeeId().equals(20L)));
    }

    @Test
    void updateUser_ShouldUpdateEnabled() {
        UserDto updateDto = new UserDto();
        updateDto.setEnabled(false);

        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(userService.updateUser(1L, updateDto))
                .assertNext(dto -> assertNotNull(dto))
                .verifyComplete();

        verify(userRepository).save(argThat(user -> !user.getEnabled()));
    }

    @Test
    void updateUser_WhenNotExists_ShouldThrowNotFound() {
        UserDto updateDto = new UserDto();
        updateDto.setRole(UserRole.MANAGER);

        when(userRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.updateUser(999L, updateDto))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    @Test
    void deleteUser_WhenExists_ShouldDelete() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(refreshTokenRepository.deleteByUserId(1L)).thenReturn(Mono.empty());
        when(userRepository.delete(any(User.class))).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUser(1L))
                .verifyComplete();

        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_WhenNotExists_ShouldThrowNotFound() {
        when(userRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUser(999L))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    @Test
    void toggleUserEnabled_ShouldEnableUser() {
        testUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(userService.toggleUserEnabled(1L, true))
                .assertNext(dto -> assertTrue(dto.getEnabled()))
                .verifyComplete();
    }

    @Test
    void toggleUserEnabled_ShouldDisableUser() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(userService.toggleUserEnabled(1L, false))
                .assertNext(dto -> assertFalse(dto.getEnabled()))
                .verifyComplete();
    }

    @Test
    void toggleUserEnabled_WhenNotExists_ShouldThrowNotFound() {
        when(userRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.toggleUserEnabled(999L, true))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }
}
