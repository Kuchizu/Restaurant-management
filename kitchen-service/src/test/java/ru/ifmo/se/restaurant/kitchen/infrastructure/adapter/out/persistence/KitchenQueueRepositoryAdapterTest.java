package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;
import ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.persistence.entity.KitchenQueueJpaEntity;
import ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.out.persistence.repository.KitchenQueueJpaRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KitchenQueueRepositoryAdapterTest {

    @Mock
    private KitchenQueueJpaRepository jpaRepository;

    @InjectMocks
    private KitchenQueueRepositoryAdapter adapter;

    private KitchenQueueJpaEntity testEntity;
    private KitchenQueue testDomain;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        testEntity = KitchenQueueJpaEntity.builder()
                .id(1L)
                .orderId(100L)
                .orderItemId(10L)
                .dishName("Pizza")
                .quantity(2)
                .status(DishStatus.PENDING)
                .createdAt(now)
                .build();

        testDomain = KitchenQueue.builder()
                .id(1L)
                .orderId(100L)
                .orderItemId(10L)
                .dishName("Pizza")
                .quantity(2)
                .status(DishStatus.PENDING)
                .createdAt(now)
                .build();
    }

    @Test
    void save_ShouldSaveAndReturnDomain() {
        when(jpaRepository.save(any(KitchenQueueJpaEntity.class))).thenReturn(testEntity);

        KitchenQueue result = adapter.save(testDomain);

        assertNotNull(result);
        assertEquals(testDomain.getId(), result.getId());
        assertEquals(testDomain.getDishName(), result.getDishName());
        verify(jpaRepository).save(any(KitchenQueueJpaEntity.class));
    }

    @Test
    void findById_ShouldReturnDomain_WhenFound() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(testEntity));

        Optional<KitchenQueue> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testDomain.getId(), result.get().getId());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        when(jpaRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<KitchenQueue> result = adapter.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllDomains() {
        when(jpaRepository.findAll()).thenReturn(Arrays.asList(testEntity));

        List<KitchenQueue> result = adapter.findAll();

        assertEquals(1, result.size());
        assertEquals(testDomain.getDishName(), result.get(0).getDishName());
    }

    @Test
    void findByStatusInOrderByCreatedAtAsc_ShouldReturnFilteredList() {
        List<DishStatus> statuses = Arrays.asList(DishStatus.PENDING, DishStatus.IN_PROGRESS);
        when(jpaRepository.findByStatusInOrderByCreatedAtAsc(statuses))
                .thenReturn(Arrays.asList(testEntity));

        List<KitchenQueue> result = adapter.findByStatusInOrderByCreatedAtAsc(statuses);

        assertEquals(1, result.size());
        verify(jpaRepository).findByStatusInOrderByCreatedAtAsc(statuses);
    }

    @Test
    void findByOrderId_ShouldReturnFilteredList() {
        when(jpaRepository.findByOrderId(100L)).thenReturn(Arrays.asList(testEntity));

        List<KitchenQueue> result = adapter.findByOrderId(100L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getOrderId());
    }

    @Test
    void findAll_WithPageable_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<KitchenQueueJpaEntity> page = new PageImpl<>(Arrays.asList(testEntity), pageable, 1);
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        Page<KitchenQueue> result = adapter.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(testDomain.getDishName(), result.getContent().get(0).getDishName());
    }

    @Test
    void findAllSlice_ShouldReturnSlice() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<KitchenQueueJpaEntity> page = new PageImpl<>(Arrays.asList(testEntity), pageable, 1);
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        Slice<KitchenQueue> result = adapter.findAllSlice(pageable);

        assertEquals(1, result.getContent().size());
        assertFalse(result.hasNext());
    }

    @Test
    void findByStatus_ShouldReturnFilteredPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<KitchenQueueJpaEntity> page = new PageImpl<>(Arrays.asList(testEntity), pageable, 1);
        when(jpaRepository.findByStatus(DishStatus.PENDING, pageable)).thenReturn(page);

        Page<KitchenQueue> result = adapter.findByStatus(DishStatus.PENDING, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(DishStatus.PENDING, result.getContent().get(0).getStatus());
    }

    @Test
    void deleteById_ShouldCallRepository() {
        doNothing().when(jpaRepository).deleteById(1L);

        adapter.deleteById(1L);

        verify(jpaRepository).deleteById(1L);
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        when(jpaRepository.existsById(1L)).thenReturn(true);

        boolean result = adapter.existsById(1L);

        assertTrue(result);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenNotExists() {
        when(jpaRepository.existsById(999L)).thenReturn(false);

        boolean result = adapter.existsById(999L);

        assertFalse(result);
    }
}
