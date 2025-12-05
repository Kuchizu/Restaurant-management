package ru.ifmo.se.restaurant.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.model.entity.Table;
import ru.ifmo.se.restaurant.model.TableStatus;

import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<Table, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Table t WHERE t.id = :id")
    Optional<Table> findByIdForUpdate(Long id);

    Optional<Table> findByTableNumber(Integer tableNumber);

    Page<Table> findByIsActiveTrue(Pageable pageable);

    Page<Table> findByStatusAndIsActiveTrue(TableStatus status, Pageable pageable);
}

