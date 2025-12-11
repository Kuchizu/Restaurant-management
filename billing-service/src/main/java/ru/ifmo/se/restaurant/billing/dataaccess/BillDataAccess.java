package ru.ifmo.se.restaurant.billing.dataaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.billing.entity.Bill;
import ru.ifmo.se.restaurant.billing.entity.BillStatus;
import ru.ifmo.se.restaurant.billing.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.billing.repository.BillRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillDataAccess {
    private final BillRepository billRepository;

    public Bill save(Bill bill) {
        log.debug("Saving bill: {}", bill);
        return billRepository.save(bill);
    }

    public Optional<Bill> findById(Long id) {
        log.debug("Finding bill by id: {}", id);
        return billRepository.findById(id);
    }

    public Bill getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
    }

    public List<Bill> findAll() {
        log.debug("Finding all bills");
        return billRepository.findAll();
    }

    public Page<Bill> findAll(Pageable pageable) {
        log.debug("Finding all bills with pagination: {}", pageable);
        return billRepository.findAll(pageable);
    }

    public Slice<Bill> findAllSlice(Pageable pageable) {
        log.debug("Finding all bills slice with pagination: {}", pageable);
        Page<Bill> page = billRepository.findAll(pageable);
        return page;
    }

    public Page<Bill> findByStatus(BillStatus status, Pageable pageable) {
        log.debug("Finding bills by status: {} with pagination: {}", status, pageable);
        return billRepository.findByStatus(status, pageable);
    }

    public Optional<Bill> findByOrderId(Long orderId) {
        log.debug("Finding bill by orderId: {}", orderId);
        return billRepository.findByOrderId(orderId);
    }

    public List<Bill> findByStatus(BillStatus status) {
        log.debug("Finding bills by status: {}", status);
        return billRepository.findByStatus(status);
    }

    public boolean existsById(Long id) {
        log.debug("Checking if bill exists by id: {}", id);
        return billRepository.existsById(id);
    }

    public void deleteById(Long id) {
        log.debug("Deleting bill by id: {}", id);
        billRepository.deleteById(id);
    }
}
