package ru.ifmo.se.restaurant.menu.dataaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.menu.entity.Ingredient;
import ru.ifmo.se.restaurant.menu.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.repository.IngredientRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngredientDataAccess {
    private final IngredientRepository ingredientRepository;

    public Ingredient save(Ingredient ingredient) {
        log.debug("Saving ingredient: {}", ingredient);
        return ingredientRepository.save(ingredient);
    }

    public Optional<Ingredient> findById(Long id) {
        log.debug("Finding ingredient by id: {}", id);
        return ingredientRepository.findById(id);
    }

    public Ingredient getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + id));
    }

    public List<Ingredient> findAll() {
        log.debug("Finding all ingredients");
        return ingredientRepository.findAll();
    }

    public List<Ingredient> findAllById(Iterable<Long> ids) {
        log.debug("Finding ingredients by ids: {}", ids);
        return ingredientRepository.findAllById(ids);
    }

    public Optional<Ingredient> findByName(String name) {
        log.debug("Finding ingredient by name: {}", name);
        return ingredientRepository.findByName(name);
    }

    public boolean existsById(Long id) {
        log.debug("Checking if ingredient exists by id: {}", id);
        return ingredientRepository.existsById(id);
    }

    public void deleteById(Long id) {
        log.debug("Deleting ingredient by id: {}", id);
        ingredientRepository.deleteById(id);
    }
}
