package ru.ifmo.se.restaurant.inventory.dataaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.inventory.entity.SupplyOrderIngredient;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.repository.SupplyOrderIngredientRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplyOrderIngredientDataAccess {
    private final SupplyOrderIngredientRepository supplyOrderIngredientRepository;

    public SupplyOrderIngredient save(SupplyOrderIngredient supplyOrderIngredient) {
        log.debug("Saving supply order ingredient: {}", supplyOrderIngredient);
        return supplyOrderIngredientRepository.save(supplyOrderIngredient);
    }

    public Optional<SupplyOrderIngredient> findById(Long id) {
        log.debug("Finding supply order ingredient by id: {}", id);
        return supplyOrderIngredientRepository.findById(id);
    }

    public SupplyOrderIngredient getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supply order ingredient not found"));
    }

    public List<SupplyOrderIngredient> findAll() {
        log.debug("Finding all supply order ingredients");
        return supplyOrderIngredientRepository.findAll();
    }

    public List<SupplyOrderIngredient> findBySupplyOrderId(Long supplyOrderId) {
        log.debug("Finding supply order ingredients by supplyOrderId: {}", supplyOrderId);
        return supplyOrderIngredientRepository.findBySupplyOrderId(supplyOrderId);
    }

    public boolean existsById(Long id) {
        log.debug("Checking if supply order ingredient exists by id: {}", id);
        return supplyOrderIngredientRepository.existsById(id);
    }

    public void deleteById(Long id) {
        log.debug("Deleting supply order ingredient by id: {}", id);
        supplyOrderIngredientRepository.deleteById(id);
    }
}
