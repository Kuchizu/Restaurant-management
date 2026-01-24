package ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.menu.application.port.out.DishRepository;
import ru.ifmo.se.restaurant.menu.domain.entity.Dish;
import ru.ifmo.se.restaurant.menu.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity.CategoryJpaEntity;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity.DishJpaEntity;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity.IngredientJpaEntity;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.repository.CategoryJpaRepository;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.repository.DishJpaRepository;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.repository.IngredientJpaRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DishRepositoryAdapter implements DishRepository {
    private final DishJpaRepository jpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final IngredientJpaRepository ingredientJpaRepository;

    @Override
    public Dish save(Dish dish) {
        log.debug("Saving dish: {}", dish);

        CategoryJpaEntity categoryEntity = categoryJpaRepository.findById(dish.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dish.getCategory().getId()));

        Set<IngredientJpaEntity> ingredientEntities = new HashSet<>();
        if (dish.getIngredients() != null && !dish.getIngredients().isEmpty()) {
            List<Long> ingredientIds = dish.getIngredients().stream()
                    .map(ing -> ing.getId())
                    .collect(Collectors.toList());
            ingredientEntities = new HashSet<>(ingredientJpaRepository.findAllById(ingredientIds));
        }

        DishJpaEntity entity = DishJpaEntity.fromDomain(dish, categoryEntity, ingredientEntities);
        DishJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dish> findById(Long id) {
        log.debug("Finding dish by id: {}", id);
        return jpaRepository.findById(id)
                .map(DishJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Dish getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dish> findAll() {
        log.debug("Finding all dishes");
        return jpaRepository.findAll().stream()
                .map(DishJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Dish> findAll(Pageable pageable) {
        log.debug("Finding all dishes with pagination: {}", pageable);
        return jpaRepository.findAll(pageable)
                .map(DishJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<Dish> findAllSlice(Pageable pageable) {
        log.debug("Finding all dishes slice with pagination: {}", pageable);
        Page<DishJpaEntity> page = jpaRepository.findAll(pageable);
        return page.map(DishJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dish> findByName(String name) {
        log.debug("Finding dish by name: {}", name);
        return jpaRepository.findByName(name)
                .map(DishJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dish> findByIsActive(Boolean isActive) {
        log.debug("Finding dishes by isActive: {}", isActive);
        return jpaRepository.findByIsActive(isActive).stream()
                .map(DishJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dish> findByCategoryId(Long categoryId) {
        log.debug("Finding dishes by categoryId: {}", categoryId);
        return jpaRepository.findByCategoryId(categoryId).stream()
                .map(DishJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dish> findActiveDishById(Long id) {
        log.debug("Finding active dish by id: {}", id);
        return jpaRepository.findActiveDishById(id)
                .map(DishJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        log.debug("Checking if dish exists by id: {}", id);
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting dish by id: {}", id);
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateImageUrl(Long dishId, String imageUrl) {
        log.debug("Updating image URL for dish {}: {}", dishId, imageUrl);
        jpaRepository.findById(dishId).ifPresent(dish -> {
            dish.setImageUrl(imageUrl);
            jpaRepository.save(dish);
        });
    }
}
