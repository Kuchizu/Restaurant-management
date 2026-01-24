package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.ifmo.se.restaurant.inventory.domain.entity.Supplier;
import ru.ifmo.se.restaurant.inventory.domain.entity.SupplyOrder;
import ru.ifmo.se.restaurant.inventory.domain.valueobject.SupplyOrderStatus;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.SupplierJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.SupplyOrderJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.SupplierJpaRepository;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.SupplyOrderJpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplyOrderRepositoryAdapterTest {

    @Mock
    private SupplyOrderJpaRepository jpaRepository;

    @Mock
    private SupplierJpaRepository supplierJpaRepository;

    private SupplyOrderRepositoryAdapter adapter;

    private SupplierJpaEntity supplierEntity;
    private SupplyOrderJpaEntity orderEntity;

    @BeforeEach
    void setUp() {
        adapter = new SupplyOrderRepositoryAdapter(jpaRepository, supplierJpaRepository);

        supplierEntity = SupplierJpaEntity.builder()
                .id(1L)
                .name("Test Supplier")
                .contactPerson("John Doe")
                .phone("123-456-7890")
                .email("supplier@test.com")
                .address("123 Test Street")
                .build();

        orderEntity = SupplyOrderJpaEntity.builder()
                .id(1L)
                .supplier(supplierEntity)
                .orderDate(LocalDateTime.now())
                .status(SupplyOrderStatus.PENDING)
                .totalCost(new BigDecimal("100.00"))
                .notes("Test order")
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void save_ShouldReturnSavedOrder() {
        when(supplierJpaRepository.findById(1L)).thenReturn(Optional.of(supplierEntity));
        when(jpaRepository.save(any())).thenReturn(orderEntity);

        Supplier supplier = Supplier.builder()
                .id(1L)
                .name("Test Supplier")
                .build();

        SupplyOrder order = SupplyOrder.builder()
                .supplier(supplier)
                .orderDate(LocalDateTime.now())
                .status(SupplyOrderStatus.PENDING)
                .totalCost(new BigDecimal("100.00"))
                .build();

        SupplyOrder result = adapter.save(order);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void save_ShouldThrowException_WhenSupplierNotFound() {
        when(supplierJpaRepository.findById(99L)).thenReturn(Optional.empty());

        Supplier supplier = Supplier.builder()
                .id(99L)
                .name("Unknown")
                .build();

        SupplyOrder order = SupplyOrder.builder()
                .supplier(supplier)
                .build();

        assertThrows(IllegalArgumentException.class, () -> adapter.save(order));
    }

    @Test
    void findById_ShouldReturnOrder_WhenExists() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(orderEntity));

        Optional<SupplyOrder> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<SupplyOrder> result = adapter.findById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        List<SupplyOrderJpaEntity> entities = List.of(orderEntity);
        when(jpaRepository.findAll()).thenReturn(entities);

        List<SupplyOrder> result = adapter.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findAllPaged_ShouldReturnPagedOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SupplyOrderJpaEntity> page = new PageImpl<>(List.of(orderEntity));
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        Page<SupplyOrder> result = adapter.findAll(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findAllSlice_ShouldReturnSlice() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SupplyOrderJpaEntity> page = new PageImpl<>(List.of(orderEntity));
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        var result = adapter.findAllSlice(pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void findByStatus_ShouldReturnFilteredOrders() {
        when(jpaRepository.findByStatus(SupplyOrderStatus.PENDING)).thenReturn(List.of(orderEntity));

        List<SupplyOrder> result = adapter.findByStatus(SupplyOrderStatus.PENDING);

        assertEquals(1, result.size());
    }

    @Test
    void findByStatus_ShouldReturnEmptyList_WhenNoMatches() {
        when(jpaRepository.findByStatus(SupplyOrderStatus.DELIVERED)).thenReturn(Collections.emptyList());

        List<SupplyOrder> result = adapter.findByStatus(SupplyOrderStatus.DELIVERED);

        assertTrue(result.isEmpty());
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        when(jpaRepository.existsById(1L)).thenReturn(true);

        assertTrue(adapter.existsById(1L));
    }

    @Test
    void existsById_ShouldReturnFalse_WhenNotExists() {
        when(jpaRepository.existsById(99L)).thenReturn(false);

        assertFalse(adapter.existsById(99L));
    }

    @Test
    void deleteById_ShouldCallRepository() {
        doNothing().when(jpaRepository).deleteById(1L);

        adapter.deleteById(1L);

        verify(jpaRepository).deleteById(1L);
    }
}
