package ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.billing.application.port.out.BillRepository;
import ru.ifmo.se.restaurant.billing.domain.entity.Bill;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;
import ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.persistence.entity.BillJpaEntity;
import ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.persistence.repository.BillJpaRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillRepositoryAdapter implements BillRepository {
    private final BillJpaRepository jpaRepository;

    @Override
    public Bill save(Bill bill) {
        log.debug("Saving bill: {}", bill);
        BillJpaEntity entity = BillJpaEntity.fromDomain(bill);
        BillJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Bill> findById(Long id) {
        log.debug("Finding bill by id: {}", id);
        return jpaRepository.findById(id)
                .map(BillJpaEntity::toDomain);
    }

    @Override
    public Optional<Bill> findByOrderId(Long orderId) {
        log.debug("Finding bill by orderId: {}", orderId);
        return jpaRepository.findByOrderId(orderId)
                .map(BillJpaEntity::toDomain);
    }

    @Override
    public List<Bill> findAll() {
        log.debug("Finding all bills");
        return jpaRepository.findAll().stream()
                .map(BillJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Page<Bill> findAll(Pageable pageable) {
        log.debug("Finding all bills with pagination: {}", pageable);
        return jpaRepository.findAll(pageable)
                .map(BillJpaEntity::toDomain);
    }

    @Override
    public Slice<Bill> findAllSlice(Pageable pageable) {
        log.debug("Finding all bills slice with pagination: {}", pageable);
        Page<BillJpaEntity> page = jpaRepository.findAll(pageable);
        return page.map(BillJpaEntity::toDomain);
    }

    @Override
    public List<Bill> findByStatus(BillStatus status) {
        log.debug("Finding bills by status: {}", status);
        return jpaRepository.findByStatus(status).stream()
                .map(BillJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Page<Bill> findByStatus(BillStatus status, Pageable pageable) {
        log.debug("Finding bills by status: {} with pagination: {}", status, pageable);
        return jpaRepository.findByStatus(status, pageable)
                .map(BillJpaEntity::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Checking if bill exists by id: {}", id);
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting bill by id: {}", id);
        jpaRepository.deleteById(id);
    }
}
