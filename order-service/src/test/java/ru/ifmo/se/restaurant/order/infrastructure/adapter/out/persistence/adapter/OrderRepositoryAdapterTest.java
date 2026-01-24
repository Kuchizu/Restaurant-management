package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.ifmo.se.restaurant.order.domain.entity.Order;
import ru.ifmo.se.restaurant.order.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.order.domain.valueobject.OrderStatus;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity.OrderJpaEntity;
import ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.repository.OrderJpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderRepositoryAdapterTest {

    @Mock
    private OrderJpaRepository jpaRepository;

    private OrderRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OrderRepositoryAdapter(jpaRepository);
    }

    private OrderJpaEntity createEntity(Long id) {
        return new OrderJpaEntity(
                id, 1L, 1L, OrderStatus.CREATED,
                new BigDecimal("100.00"), null,
                LocalDateTime.now(), null, 0L
        );
    }

    @Test
    void findById_ShouldReturnOrder_WhenExists() {
        OrderJpaEntity entity = createEntity(1L);
        when(jpaRepository.findById(1L)).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.findById(1L))
                .expectNextMatches(order -> order.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        when(jpaRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(99L))
                .verifyComplete();
    }

    @Test
    void getById_ShouldReturnOrder_WhenExists() {
        OrderJpaEntity entity = createEntity(1L);
        when(jpaRepository.findById(1L)).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.getById(1L))
                .expectNextMatches(order -> order.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void getById_ShouldThrowException_WhenNotExists() {
        when(jpaRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.getById(99L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        when(jpaRepository.findAll()).thenReturn(Flux.just(createEntity(1L), createEntity(2L)));

        StepVerifier.create(adapter.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findAllPaged_ShouldReturnPagedOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        when(jpaRepository.findAll()).thenReturn(Flux.just(createEntity(1L)));

        StepVerifier.create(adapter.findAll(pageable))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findAllPaged_WithSort_ShouldReturnSortedOrders() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        when(jpaRepository.findAll()).thenReturn(Flux.just(createEntity(1L), createEntity(2L)));

        StepVerifier.create(adapter.findAll(pageable))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void count_ShouldReturnCount() {
        when(jpaRepository.count()).thenReturn(Mono.just(5L));

        StepVerifier.create(adapter.count())
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void save_ShouldReturnSavedOrder() {
        Order order = new Order(null, 1L, 1L, OrderStatus.CREATED,
                new BigDecimal("100.00"), null, LocalDateTime.now(), null, null);
        OrderJpaEntity savedEntity = createEntity(1L);
        when(jpaRepository.save(any())).thenReturn(Mono.just(savedEntity));

        StepVerifier.create(adapter.save(order))
                .expectNextMatches(o -> o.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void deleteById_ShouldComplete() {
        when(jpaRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(1L))
                .verifyComplete();

        verify(jpaRepository).deleteById(1L);
    }
}
