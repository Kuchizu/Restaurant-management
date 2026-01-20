package ru.ifmo.se.restaurant.inventory.domain.entity;

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
    private final String description;

    public Ingredient updateInfo(String newName, String newUnit, String newDescription) {
        return new Ingredient(
            id,
            newName != null ? newName : name,
            newUnit != null ? newUnit : unit,
            newDescription != null ? newDescription : description
        );
    }
}
