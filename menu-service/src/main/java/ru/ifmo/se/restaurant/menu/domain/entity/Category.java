package ru.ifmo.se.restaurant.menu.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Category {
    private final Long id;
    private final String name;
    private final String description;
    private final Boolean isActive;

    public boolean isAvailable() {
        return isActive != null && isActive;
    }
}
