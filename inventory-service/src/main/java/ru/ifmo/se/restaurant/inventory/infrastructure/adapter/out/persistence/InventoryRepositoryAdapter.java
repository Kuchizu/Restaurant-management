package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.inventory.application.port.out.InventoryRepository;
import ru.ifmo.se.restaurant.inventory.domain.entity.Inventory;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.IngredientJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.InventoryJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.IngredientJpaRepository;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.InventoryJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepository {

    private final InventoryJpaRepository jpaRepository;
    private final IngredientJpaRepository ingredientJpaRepository;

    @Override
    public Inventory save(Inventory inventory) {
        log.debug("Saving inventory: {}", inventory);
        IngredientJpaEntity ingredientEntity = ingredientJpaRepository.findById(inventory.getIngredient().getId())
            .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));

        InventoryJpaEntity entity = InventoryJpaEntity.fromDomain(inventory, ingredientEntity);
        InventoryJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        log.debug("Finding inventory by id: {}", id);
        return jpaRepository.findById(id)
            .map(InventoryJpaEntity::toDomain);
    }

    @Override
    public List<Inventory> findAll() {
        log.debug("Finding all inventory items");
        return jpaRepository.findAll().stream()
            .map(InventoryJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<Inventory> findAll(Pageable pageable) {
        log.debug("Finding all inventory items with pagination: {}", pageable);
        return jpaRepository.findAll(pageable)
            .map(InventoryJpaEntity::toDomain);
    }

    @Override
    public Slice<Inventory> findAllSlice(Pageable pageable) {
        log.debug("Finding all inventory items slice with pagination: {}", pageable);
        Page<InventoryJpaEntity> page = jpaRepository.findAll(pageable);
        return page.map(InventoryJpaEntity::toDomain);
    }

    @Override
    public Optional<Inventory> findByIngredientId(Long ingredientId) {
        log.debug("Finding inventory by ingredientId: {}", ingredientId);
        return jpaRepository.findByIngredientId(ingredientId)
            .map(InventoryJpaEntity::toDomain);
    }

    @Override
    public List<Inventory> findLowStockItems() {
        log.debug("Finding low stock inventory items");
        return jpaRepository.findLowStockItems().stream()
            .map(InventoryJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Checking if inventory exists by id: {}", id);
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting inventory by id: {}", id);
        jpaRepository.deleteById(id);
    }
}
