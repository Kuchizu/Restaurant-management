package ru.ifmo.se.restaurant.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent<T> {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private T payload;

    public static <T> DomainEvent<T> create(String eventType, T payload, String correlationId) {
        return new DomainEvent<>(
                UUID.randomUUID().toString(),
                eventType,
                Instant.now(),
                correlationId,
                payload
        );
    }

    public static <T> DomainEvent<T> create(String eventType, T payload) {
        return create(eventType, payload, UUID.randomUUID().toString());
    }
}
