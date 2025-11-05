package ru.ifmo.se.restaurant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.model.entity.Inventory;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Page<Inventory> findAll(Pageable pageable);
    
    @Query("SELECT i FROM Inventory i WHERE i.ingredient.id = :ingredientId")
    List<Inventory> findByIngredientId(@Param("ingredientId") Long ingredientId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.ingredient.id = :ingredientId AND (i.quantity - i.reservedQuantity) >= :requiredQuantity AND i.expiryDate > :currentDate ORDER BY i.expiryDate ASC")
    List<Inventory> findAvailableForReservation(
        @Param("ingredientId") Long ingredientId,
        @Param("requiredQuantity") Integer requiredQuantity,
        @Param("currentDate") LocalDate currentDate
    );
    
    @Query("SELECT i FROM Inventory i WHERE i.expiryDate <= :date AND (i.quantity - i.reservedQuantity) > 0")
    List<Inventory> findExpiringSoon(@Param("date") LocalDate date);
    
    @Query("SELECT SUM(i.quantity - i.reservedQuantity) FROM Inventory i WHERE i.ingredient.id = :ingredientId AND i.expiryDate > :currentDate")
    Optional<Integer> getAvailableQuantity(@Param("ingredientId") Long ingredientId, @Param("currentDate") LocalDate currentDate);
}

