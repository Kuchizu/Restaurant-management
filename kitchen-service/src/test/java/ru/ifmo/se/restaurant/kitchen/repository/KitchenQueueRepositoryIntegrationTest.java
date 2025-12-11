package ru.ifmo.se.restaurant.kitchen.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.entity.KitchenQueue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class KitchenQueueRepositoryIntegrationTest {

    @Autowired
    private KitchenQueueRepository kitchenQueueRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_PersistsKitchenQueue() {
        // Given
        KitchenQueue queue = new KitchenQueue();
        queue.setOrderId(100L);
        queue.setOrderItemId(200L);
        queue.setDishName("Test Dish");
        queue.setQuantity(2);
        queue.setStatus(DishStatus.PENDING);
        queue.setSpecialRequest("No onions");
        queue.setCreatedAt(LocalDateTime.now());

        // When
        KitchenQueue savedQueue = kitchenQueueRepository.save(queue);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(savedQueue.getId()).isNotNull();
        Optional<KitchenQueue> found = kitchenQueueRepository.findById(savedQueue.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDishName()).isEqualTo("Test Dish");
        assertThat(found.get().getQuantity()).isEqualTo(2);
        assertThat(found.get().getStatus()).isEqualTo(DishStatus.PENDING);
    }

    @Test
    void findById_ReturnsCorrectQueue() {
        // Given
        KitchenQueue queue = createAndPersistQueue("Burger", DishStatus.PENDING);

        // When
        Optional<KitchenQueue> found = kitchenQueueRepository.findById(queue.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDishName()).isEqualTo("Burger");
        assertThat(found.get().getStatus()).isEqualTo(DishStatus.PENDING);
    }

    @Test
    void findByStatusInOrderByCreatedAtAsc_ReturnsQueueInCorrectOrder() {
        // Given
        KitchenQueue queue1 = createAndPersistQueueWithDelay("Dish 1", DishStatus.PENDING, 0);
        KitchenQueue queue2 = createAndPersistQueueWithDelay("Dish 2", DishStatus.IN_PROGRESS, 1);
        KitchenQueue queue3 = createAndPersistQueueWithDelay("Dish 3", DishStatus.PENDING, 2);
        KitchenQueue queue4 = createAndPersistQueueWithDelay("Dish 4", DishStatus.READY, 3);

        entityManager.flush();
        entityManager.clear();

        // When
        List<DishStatus> activeStatuses = Arrays.asList(DishStatus.PENDING, DishStatus.IN_PROGRESS);
        List<KitchenQueue> result = kitchenQueueRepository.findByStatusInOrderByCreatedAtAsc(activeStatuses);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getDishName()).isEqualTo("Dish 1");
        assertThat(result.get(1).getDishName()).isEqualTo("Dish 2");
        assertThat(result.get(2).getDishName()).isEqualTo("Dish 3");
        assertThat(result).allMatch(q ->
                q.getStatus() == DishStatus.PENDING || q.getStatus() == DishStatus.IN_PROGRESS
        );
    }

    @Test
    void findByStatusInOrderByCreatedAtAsc_WhenNoMatches_ReturnsEmptyList() {
        // Given
        createAndPersistQueue("Dish 1", DishStatus.READY);
        createAndPersistQueue("Dish 2", DishStatus.SERVED);
        entityManager.flush();

        // When
        List<DishStatus> activeStatuses = Arrays.asList(DishStatus.PENDING, DishStatus.IN_PROGRESS);
        List<KitchenQueue> result = kitchenQueueRepository.findByStatusInOrderByCreatedAtAsc(activeStatuses);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByOrderId_ReturnsAllQueuesForOrder() {
        // Given
        Long orderId = 100L;
        KitchenQueue queue1 = createAndPersistQueueForOrder(orderId, "Dish 1");
        KitchenQueue queue2 = createAndPersistQueueForOrder(orderId, "Dish 2");
        KitchenQueue queue3 = createAndPersistQueueForOrder(999L, "Dish 3");

        entityManager.flush();
        entityManager.clear();

        // When
        List<KitchenQueue> result = kitchenQueueRepository.findByOrderId(orderId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(q -> q.getOrderId().equals(orderId));
        assertThat(result).extracting(KitchenQueue::getDishName)
                .containsExactlyInAnyOrder("Dish 1", "Dish 2");
    }

    @Test
    void findByOrderId_WhenNoMatches_ReturnsEmptyList() {
        // Given
        createAndPersistQueueForOrder(100L, "Dish 1");
        entityManager.flush();

        // When
        List<KitchenQueue> result = kitchenQueueRepository.findByOrderId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void update_UpdatesQueueStatus() {
        // Given
        KitchenQueue queue = createAndPersistQueue("Dish", DishStatus.PENDING);
        entityManager.flush();
        entityManager.clear();

        // When
        KitchenQueue foundQueue = kitchenQueueRepository.findById(queue.getId()).get();
        foundQueue.setStatus(DishStatus.IN_PROGRESS);
        foundQueue.setStartedAt(LocalDateTime.now());
        kitchenQueueRepository.save(foundQueue);
        entityManager.flush();
        entityManager.clear();

        // Then
        KitchenQueue updatedQueue = kitchenQueueRepository.findById(queue.getId()).get();
        assertThat(updatedQueue.getStatus()).isEqualTo(DishStatus.IN_PROGRESS);
        assertThat(updatedQueue.getStartedAt()).isNotNull();
    }

    @Test
    void delete_RemovesQueue() {
        // Given
        KitchenQueue queue = createAndPersistQueue("Dish", DishStatus.PENDING);
        Long queueId = queue.getId();
        entityManager.flush();
        entityManager.clear();

        // When
        kitchenQueueRepository.deleteById(queueId);
        entityManager.flush();

        // Then
        Optional<KitchenQueue> found = kitchenQueueRepository.findById(queueId);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ReturnsAllQueues() {
        // Given
        createAndPersistQueue("Dish 1", DishStatus.PENDING);
        createAndPersistQueue("Dish 2", DishStatus.IN_PROGRESS);
        createAndPersistQueue("Dish 3", DishStatus.READY);
        entityManager.flush();

        // When
        List<KitchenQueue> result = kitchenQueueRepository.findAll();

        // Then
        assertThat(result).hasSizeGreaterThanOrEqualTo(3);
    }

    // Helper methods
    private KitchenQueue createAndPersistQueue(String dishName, DishStatus status) {
        KitchenQueue queue = new KitchenQueue();
        queue.setOrderId(100L);
        queue.setOrderItemId(200L);
        queue.setDishName(dishName);
        queue.setQuantity(1);
        queue.setStatus(status);
        queue.setCreatedAt(LocalDateTime.now());
        return entityManager.persistAndFlush(queue);
    }

    private KitchenQueue createAndPersistQueueWithDelay(String dishName, DishStatus status, int secondsDelay) {
        KitchenQueue queue = new KitchenQueue();
        queue.setOrderId(100L);
        queue.setOrderItemId(200L);
        queue.setDishName(dishName);
        queue.setQuantity(1);
        queue.setStatus(status);
        queue.setCreatedAt(LocalDateTime.now().minusSeconds(10 - secondsDelay));
        return entityManager.persistAndFlush(queue);
    }

    private KitchenQueue createAndPersistQueueForOrder(Long orderId, String dishName) {
        KitchenQueue queue = new KitchenQueue();
        queue.setOrderId(orderId);
        queue.setOrderItemId(orderId * 10);
        queue.setDishName(dishName);
        queue.setQuantity(1);
        queue.setStatus(DishStatus.PENDING);
        queue.setCreatedAt(LocalDateTime.now());
        return entityManager.persistAndFlush(queue);
    }
}
