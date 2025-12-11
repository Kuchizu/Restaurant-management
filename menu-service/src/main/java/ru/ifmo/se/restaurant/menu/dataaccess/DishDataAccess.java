package ru.ifmo.se.restaurant.menu.dataaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.menu.entity.Dish;
import ru.ifmo.se.restaurant.menu.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.repository.DishRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DishDataAccess {
    private final DishRepository dishRepository;

    public Dish save(Dish dish) {
        log.debug("Saving dish: {}", dish);
        return dishRepository.save(dish);
    }

    public Optional<Dish> findById(Long id) {
        log.debug("Finding dish by id: {}", id);
        return dishRepository.findById(id);
    }

    public Dish getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + id));
    }

    public List<Dish> findAll() {
        log.debug("Finding all dishes");
        return dishRepository.findAll();
    }

    public Page<Dish> findAll(Pageable pageable) {
        log.debug("Finding all dishes with pagination: {}", pageable);
        return dishRepository.findAll(pageable);
    }

    public Slice<Dish> findAllSlice(Pageable pageable) {
        log.debug("Finding all dishes slice with pagination: {}", pageable);
        Page<Dish> page = dishRepository.findAll(pageable);
        return page;
    }

    public Optional<Dish> findByName(String name) {
        log.debug("Finding dish by name: {}", name);
        return dishRepository.findByName(name);
    }

    public List<Dish> findByIsActive(Boolean isActive) {
        log.debug("Finding dishes by isActive: {}", isActive);
        return dishRepository.findByIsActive(isActive);
    }

    public List<Dish> findByCategoryId(Long categoryId) {
        log.debug("Finding dishes by categoryId: {}", categoryId);
        return dishRepository.findByCategoryId(categoryId);
    }

    public Optional<Dish> findActiveDishById(Long id) {
        log.debug("Finding active dish by id: {}", id);
        return dishRepository.findActiveDishById(id);
    }

    public boolean existsById(Long id) {
        log.debug("Checking if dish exists by id: {}", id);
        return dishRepository.existsById(id);
    }

    public void deleteById(Long id) {
        log.debug("Deleting dish by id: {}", id);
        dishRepository.deleteById(id);
    }
}
