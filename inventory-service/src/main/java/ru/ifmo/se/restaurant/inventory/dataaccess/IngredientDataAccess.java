package ru.ifmo.se.restaurant.inventory.dataaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.inventory.entity.Ingredient;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.repository.IngredientRepository;

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
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
    }

    public List<Ingredient> findAll() {
        log.debug("Finding all ingredients");
        return ingredientRepository.findAll();
    }

    public Page<Ingredient> findAll(Pageable pageable) {
        log.debug("Finding all ingredients with pagination: {}", pageable);
        return ingredientRepository.findAll(pageable);
    }

    public Slice<Ingredient> findAllSlice(Pageable pageable) {
        log.debug("Finding all ingredients slice with pagination: {}", pageable);
        Page<Ingredient> page = ingredientRepository.findAll(pageable);
        return page;
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
