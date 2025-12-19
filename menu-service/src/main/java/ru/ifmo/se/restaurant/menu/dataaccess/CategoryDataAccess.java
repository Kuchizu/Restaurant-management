package ru.ifmo.se.restaurant.menu.dataaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.menu.entity.Category;
import ru.ifmo.se.restaurant.menu.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryDataAccess {
    private final CategoryRepository categoryRepository;

    public Category save(Category category) {
        log.debug("Saving category: {}", category);
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        log.debug("Finding category by id: {}", id);
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        log.debug("Finding all categories");
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Category> findAll(Pageable pageable) {
        log.debug("Finding all categories with pagination: {}", pageable);
        return categoryRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Category> findAllSlice(Pageable pageable) {
        log.debug("Finding all categories slice with pagination: {}", pageable);
        Page<Category> page = categoryRepository.findAll(pageable);
        return page;
    }

    @Transactional(readOnly = true)
    public Optional<Category> findByName(String name) {
        log.debug("Finding category by name: {}", name);
        return categoryRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<Category> findByIsActive(Boolean isActive) {
        log.debug("Finding categories by isActive: {}", isActive);
        return categoryRepository.findByIsActive(isActive);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        log.debug("Checking if category exists by id: {}", id);
        return categoryRepository.existsById(id);
    }

    public void deleteById(Long id) {
        log.debug("Deleting category by id: {}", id);
        categoryRepository.deleteById(id);
    }
}
