package ru.ifmo.se.restaurant.menu.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Ingredient {
    private final Long id;
    private final String name;
    private final String unit;

    public String getDisplayName() {
        if (unit != null && !unit.isEmpty()) {
            return name + " (" + unit + ")";
        }
        return name;
    }
}
