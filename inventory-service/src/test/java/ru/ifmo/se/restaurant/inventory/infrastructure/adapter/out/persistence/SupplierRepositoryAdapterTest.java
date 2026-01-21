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
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.SupplierJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.SupplierJpaRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierRepositoryAdapterTest {

    @Mock
    private SupplierJpaRepository jpaRepository;

    private SupplierRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SupplierRepositoryAdapter(jpaRepository);
    }

    private SupplierJpaEntity createSupplierEntity(Long id, String name) {
        return SupplierJpaEntity.builder()
                .id(id)
                .name(name)
                .contactPerson("John Doe")
                .phone("123-456-7890")
                .email("supplier@test.com")
                .address("123 Test Street")
                .build();
    }

    @Test
    void save_ShouldReturnSavedSupplier() {
        SupplierJpaEntity entity = createSupplierEntity(1L, "Test Supplier");
        when(jpaRepository.save(any())).thenReturn(entity);

        Supplier supplier = Supplier.builder()
                .name("Test Supplier")
                .contactPerson("John Doe")
                .phone("123-456-7890")
                .email("supplier@test.com")
                .address("123 Test Street")
                .build();

        Supplier result = adapter.save(supplier);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Supplier", result.getName());
    }

    @Test
    void findById_ShouldReturnSupplier_WhenExists() {
        SupplierJpaEntity entity = createSupplierEntity(1L, "Test Supplier");
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<Supplier> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Supplier", result.get().getName());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Supplier> result = adapter.findById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllSuppliers() {
        List<SupplierJpaEntity> entities = List.of(createSupplierEntity(1L, "Supplier 1"));
        when(jpaRepository.findAll()).thenReturn(entities);

        List<Supplier> result = adapter.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findAllPaged_ShouldReturnPagedSuppliers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SupplierJpaEntity> page = new PageImpl<>(List.of(createSupplierEntity(1L, "Supplier 1")));
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        Page<Supplier> result = adapter.findAll(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findAllSlice_ShouldReturnSlice() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SupplierJpaEntity> page = new PageImpl<>(List.of(createSupplierEntity(1L, "Supplier 1")));
        when(jpaRepository.findAll(pageable)).thenReturn(page);

        var result = adapter.findAllSlice(pageable);

        assertEquals(1, result.getContent().size());
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
