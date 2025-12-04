package ru.ifmo.se.restaurant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.model.entity.Dish;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    @EntityGraph(attributePaths = {"category", "ingredients"})
    Optional<Dish> findById(Long id);

    @EntityGraph(attributePaths = {"category", "ingredients"})
    Page<Dish> findByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "ingredients"})
    Page<Dish> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "ingredients"})
    @Query("SELECT d FROM Dish d WHERE d.isActive = true")
    List<Dish> findAllActive();

    @EntityGraph(attributePaths = {"category", "ingredients"})
    Optional<Dish> findByIdAndIsActiveTrue(Long id);
}

