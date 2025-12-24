package ru.ifmo.se.restaurant.gateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.gateway.dto.RegisterRequest;
import ru.ifmo.se.restaurant.gateway.dto.UserDto;
import ru.ifmo.se.restaurant.gateway.entity.User;
import ru.ifmo.se.restaurant.gateway.repository.RefreshTokenRepository;
import ru.ifmo.se.restaurant.gateway.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public Flux<UserDto> getAllUsers() {
        return userRepository.findAll().map(this::toUserDto);
    }

    public Mono<Page<UserDto>> getAllUsersPaginated(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return userRepository.findAll()
                .collectList()
                .map(users -> {
                    int start = (int) pageRequest.getOffset();
                    int end = Math.min(start + pageRequest.getPageSize(), users.size());
                    var pageContent = users.subList(start, end).stream()
                            .map(this::toUserDto)
                            .toList();
                    return new PageImpl<>(pageContent, pageRequest, users.size());
                });
    }

    public Mono<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .map(this::toUserDto);
    }

    public Mono<UserDto> createUser(RegisterRequest request) {
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

    public Mono<UserDto> updateUser(Long id, UserDto dto) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(user -> {
                    if (dto.getRole() != null) {
                        user.setRole(dto.getRole());
                    }
                    if (dto.getEmployeeId() != null) {
                        user.setEmployeeId(dto.getEmployeeId());
                    }
                    if (dto.getEnabled() != null) {
                        user.setEnabled(dto.getEnabled());
                    }
                    return userRepository.save(user);
                })
                .map(this::toUserDto);
    }

    public Mono<Void> deleteUser(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(user -> refreshTokenRepository.deleteByUserId(id)
                        .then(userRepository.delete(user)));
    }

    public Mono<UserDto> toggleUserEnabled(Long id, boolean enabled) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(user -> {
                    user.setEnabled(enabled);
                    return userRepository.save(user);
                })
                .map(this::toUserDto);
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
