package ru.ifmo.se.restaurant.inventory.application.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.inventory.domain.entity.Ingredient;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository {
    Ingredient save(Ingredient ingredient);
    Optional<Ingredient> findById(Long id);
    List<Ingredient> findAll();
    Page<Ingredient> findAll(Pageable pageable);
    Slice<Ingredient> findAllSlice(Pageable pageable);
    boolean existsById(Long id);
    void deleteById(Long id);
}
