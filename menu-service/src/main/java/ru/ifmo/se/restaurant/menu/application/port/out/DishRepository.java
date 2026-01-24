package ru.ifmo.se.restaurant.menu.application.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.menu.domain.entity.Dish;

import java.util.List;
import java.util.Optional;

public interface DishRepository {
    Dish save(Dish dish);
    Optional<Dish> findById(Long id);
    Dish getById(Long id);
    List<Dish> findAll();
    Page<Dish> findAll(Pageable pageable);
    Slice<Dish> findAllSlice(Pageable pageable);
    Optional<Dish> findByName(String name);
    List<Dish> findByIsActive(Boolean isActive);
    List<Dish> findByCategoryId(Long categoryId);
    Optional<Dish> findActiveDishById(Long id);
    boolean existsById(Long id);
    void deleteById(Long id);
    void updateImageUrl(Long dishId, String imageUrl);
}
