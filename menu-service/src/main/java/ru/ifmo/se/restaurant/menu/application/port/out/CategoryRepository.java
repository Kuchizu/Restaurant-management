package ru.ifmo.se.restaurant.menu.application.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.menu.domain.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(Long id);
    Category getById(Long id);
    List<Category> findAll();
    Page<Category> findAll(Pageable pageable);
    Slice<Category> findAllSlice(Pageable pageable);
    Optional<Category> findByName(String name);
    List<Category> findByIsActive(Boolean isActive);
    boolean existsById(Long id);
    void deleteById(Long id);
}
