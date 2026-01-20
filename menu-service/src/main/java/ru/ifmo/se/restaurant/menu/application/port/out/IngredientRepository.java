package ru.ifmo.se.restaurant.menu.application.port.out;

import ru.ifmo.se.restaurant.menu.domain.entity.Ingredient;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository {
    Ingredient save(Ingredient ingredient);
    Optional<Ingredient> findById(Long id);
    Ingredient getById(Long id);
    List<Ingredient> findAll();
    List<Ingredient> findAllById(List<Long> ids);
    Optional<Ingredient> findByName(String name);
}
