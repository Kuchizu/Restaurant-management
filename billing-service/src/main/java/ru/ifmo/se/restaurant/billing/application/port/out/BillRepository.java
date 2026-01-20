package ru.ifmo.se.restaurant.billing.application.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.billing.domain.entity.Bill;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;

import java.util.List;
import java.util.Optional;

public interface BillRepository {
    Bill save(Bill bill);
    Optional<Bill> findById(Long id);
    Optional<Bill> findByOrderId(Long orderId);
    List<Bill> findAll();
    Page<Bill> findAll(Pageable pageable);
    Slice<Bill> findAllSlice(Pageable pageable);
    List<Bill> findByStatus(BillStatus status);
    Page<Bill> findByStatus(BillStatus status, Pageable pageable);
    boolean existsById(Long id);
    void deleteById(Long id);
}
