package ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.ifmo.se.restaurant.billing.domain.entity.Bill;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;
import ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.persistence.entity.BillJpaEntity;
import ru.ifmo.se.restaurant.billing.infrastructure.adapter.out.persistence.repository.BillJpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillRepositoryAdapterTest {

    @Mock
    private BillJpaRepository jpaRepository;

    @InjectMocks
    private BillRepositoryAdapter adapter;

    private BillJpaEntity testEntity;
    private Bill testDomain;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        testEntity = BillJpaEntity.builder()
                .id(1L)
                .orderId(100L)
                .totalAmount(new BigDecimal("100.00"))
                .finalAmount(new BigDecimal("115.00"))
                .status(BillStatus.PENDING)
                .createdAt(now)
                .build();

        testDomain = Bill.builder()
                .id(1L)
                .orderId(100L)
                .totalAmount(new BigDecimal("100.00"))
                .finalAmount(new BigDecimal("115.00"))
                .status(BillStatus.PENDING)
                .createdAt(now)
                .build();
    }

    @Test
    void save_ShouldSaveAndReturnDomain() {
        when(jpaRepository.save(any(BillJpaEntity.class))).thenReturn(testEntity);

        Bill result = adapter.save(testDomain);

        assertNotNull(result);
        assertEquals(testDomain.getId(), result.getId());
        verify(jpaRepository).save(any(BillJpaEntity.class));
    }

    @Test
    void findById_ShouldReturnDomain_WhenFound() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(testEntity));

        Optional<Bill> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testDomain.getId(), result.get().getId());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        when(jpaRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Bill> result = adapter.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void findByOrderId_ShouldReturnDomain_WhenFound() {
        when(jpaRepository.findByOrderId(100L)).thenReturn(Optional.of(testEntity));

        Optional<Bill> result = adapter.findByOrderId(100L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getOrderId());
    }

    @Test
    void findAll_ShouldReturnAllDomains() {
        when(jpaRepository.findAll()).thenReturn(Arrays.asList(testEntity));

        List<Bill> result = adapter.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findAll_WithPageable_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BillJpaEntity> page = new PageImpl<>(Arrays.asList(testEntity), pageable, 1);
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        Page<Bill> result = adapter.findAll(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findAllSlice_ShouldReturnSlice() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BillJpaEntity> page = new PageImpl<>(Arrays.asList(testEntity), pageable, 1);
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        Slice<Bill> result = adapter.findAllSlice(pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void findByStatus_ShouldReturnFilteredList() {
        when(jpaRepository.findByStatus(BillStatus.PENDING)).thenReturn(Arrays.asList(testEntity));

        List<Bill> result = adapter.findByStatus(BillStatus.PENDING);

        assertEquals(1, result.size());
        assertEquals(BillStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    void findByStatus_WithPageable_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BillJpaEntity> page = new PageImpl<>(Arrays.asList(testEntity), pageable, 1);
        when(jpaRepository.findByStatus(BillStatus.PENDING, pageable)).thenReturn(page);

        Page<Bill> result = adapter.findByStatus(BillStatus.PENDING, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        when(jpaRepository.existsById(1L)).thenReturn(true);

        assertTrue(adapter.existsById(1L));
    }

    @Test
    void existsById_ShouldReturnFalse_WhenNotExists() {
        when(jpaRepository.existsById(999L)).thenReturn(false);

        assertFalse(adapter.existsById(999L));
    }

    @Test
    void deleteById_ShouldCallRepository() {
        doNothing().when(jpaRepository).deleteById(1L);

        adapter.deleteById(1L);

        verify(jpaRepository).deleteById(1L);
    }
}
