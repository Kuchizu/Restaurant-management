package ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.menu.application.port.out.IngredientRepository;
import ru.ifmo.se.restaurant.menu.domain.entity.Ingredient;
import ru.ifmo.se.restaurant.menu.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity.IngredientJpaEntity;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.repository.IngredientJpaRepository;

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
    @Transactional(readOnly = true)
    public Optional<Ingredient> findById(Long id) {
        log.debug("Finding ingredient by id: {}", id);
        return jpaRepository.findById(id)
                .map(IngredientJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Ingredient getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ingredient> findAll() {
        log.debug("Finding all ingredients");
        return jpaRepository.findAll().stream()
                .map(IngredientJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ingredient> findAllById(List<Long> ids) {
        log.debug("Finding ingredients by ids: {}", ids);
        return jpaRepository.findAllById(ids).stream()
                .map(IngredientJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ingredient> findByName(String name) {
        log.debug("Finding ingredient by name: {}", name);
        return jpaRepository.findByName(name)
                .map(IngredientJpaEntity::toDomain);
    }
}
