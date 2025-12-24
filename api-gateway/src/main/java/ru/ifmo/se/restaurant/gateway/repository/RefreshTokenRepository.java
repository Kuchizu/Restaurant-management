package ru.ifmo.se.restaurant.gateway.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.gateway.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends R2dbcRepository<RefreshToken, Long> {
    Mono<RefreshToken> findByToken(String token);
    Mono<Void> deleteByUserId(Long userId);
    Mono<Void> deleteByToken(String token);
}
