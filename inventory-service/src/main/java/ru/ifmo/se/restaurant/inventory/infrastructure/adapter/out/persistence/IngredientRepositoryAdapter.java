package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.inventory.application.port.out.IngredientRepository;
import ru.ifmo.se.restaurant.inventory.domain.entity.Ingredient;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.IngredientJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.IngredientJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngredientRepositoryAdapter implements IngredientRepository {

    private final IngredientJpaRepository jpaRepository;

    @Override
    public Ingredient save(Ingredient ingredient) {
        log.debug("Saving ingredient: {}", ingredient);
        IngredientJpaEntity entity = IngredientJpaEntity.fromDomain(ingredient);
        IngredientJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Ingredient> findById(Long id) {
        log.debug("Finding ingredient by id: {}", id);
        return jpaRepository.findById(id)
            .map(IngredientJpaEntity::toDomain);
    }

    @Override
    public List<Ingredient> findAll() {
        log.debug("Finding all ingredients");
        return jpaRepository.findAll().stream()
            .map(IngredientJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<Ingredient> findAll(Pageable pageable) {
        log.debug("Finding all ingredients with pagination: {}", pageable);
        return jpaRepository.findAll(pageable)
            .map(IngredientJpaEntity::toDomain);
    }

    @Override
    public Slice<Ingredient> findAllSlice(Pageable pageable) {
        log.debug("Finding all ingredients slice with pagination: {}", pageable);
        Page<IngredientJpaEntity> page = jpaRepository.findAll(pageable);
        return page.map(IngredientJpaEntity::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Checking if ingredient exists by id: {}", id);
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting ingredient by id: {}", id);
        jpaRepository.deleteById(id);
    }
}
