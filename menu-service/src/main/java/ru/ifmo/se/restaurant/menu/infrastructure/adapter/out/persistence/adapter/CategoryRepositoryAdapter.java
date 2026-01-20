package ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.menu.application.port.out.CategoryRepository;
import ru.ifmo.se.restaurant.menu.domain.entity.Category;
import ru.ifmo.se.restaurant.menu.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.entity.CategoryJpaEntity;
import ru.ifmo.se.restaurant.menu.infrastructure.adapter.out.persistence.repository.CategoryJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {
    private final CategoryJpaRepository jpaRepository;

    @Override
    public Category save(Category category) {
        log.debug("Saving category: {}", category);
        CategoryJpaEntity entity = CategoryJpaEntity.fromDomain(category);
        CategoryJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        log.debug("Finding category by id: {}", id);
        return jpaRepository.findById(id)
                .map(CategoryJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        log.debug("Finding all categories");
        return jpaRepository.findAll().stream()
                .map(CategoryJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> findAll(Pageable pageable) {
        log.debug("Finding all categories with pagination: {}", pageable);
        return jpaRepository.findAll(pageable)
                .map(CategoryJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<Category> findAllSlice(Pageable pageable) {
        log.debug("Finding all categories slice with pagination: {}", pageable);
        Page<CategoryJpaEntity> page = jpaRepository.findAll(pageable);
        return page.map(CategoryJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findByName(String name) {
        log.debug("Finding category by name: {}", name);
        return jpaRepository.findByName(name)
                .map(CategoryJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findByIsActive(Boolean isActive) {
        log.debug("Finding categories by isActive: {}", isActive);
        return jpaRepository.findByIsActive(isActive).stream()
                .map(CategoryJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        log.debug("Checking if category exists by id: {}", id);
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting category by id: {}", id);
        jpaRepository.deleteById(id);
    }
}
