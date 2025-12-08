package ru.ifmo.se.restaurant.menu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.menu.entity.Dish;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    Optional<Dish> findByName(String name);
    List<Dish> findByIsActive(Boolean isActive);
    List<Dish> findByCategoryId(Long categoryId);

    @Query("SELECT d FROM Dish d WHERE d.isActive = true AND d.id = :id")
    Optional<Dish> findActiveDishById(@Param("id") Long id);
}
