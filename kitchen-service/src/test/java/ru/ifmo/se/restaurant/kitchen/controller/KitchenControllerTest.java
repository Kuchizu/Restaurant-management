package ru.ifmo.se.restaurant.kitchen.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.kitchen.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.kitchen.service.KitchenService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebFluxTest(KitchenController.class)
class KitchenControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private KitchenService kitchenService;

    private KitchenQueueDto createMockKitchenQueueDto(Long id) {
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setId(id);
        dto.setOrderId(100L);
        dto.setOrderItemId(200L);
        dto.setDishName("Test Dish");
        dto.setQuantity(2);
        dto.setStatus(DishStatus.PENDING);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }

    private KitchenQueueDto createKitchenQueueDtoForCreation() {
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setOrderId(100L);
        dto.setOrderItemId(200L);
        dto.setDishName("Test Dish");
        dto.setQuantity(2);
        return dto;
    }

    @Test
    void addToQueue_CreatesNewQueueItem() {
        // Given
        KitchenQueueDto inputDto = createKitchenQueueDtoForCreation();
        KitchenQueueDto responseDto = createMockKitchenQueueDto(1L);
        responseDto.setDishName("New Dish");

        when(kitchenService.addToQueue(any(KitchenQueueDto.class))).thenReturn(Mono.just(responseDto));

        // When & Then
        webTestClient.post()
                .uri("/api/kitchen/queue")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inputDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.orderId").isEqualTo(100)
                .jsonPath("$.dishName").isEqualTo("New Dish")
                .jsonPath("$.status").isEqualTo("PENDING");

        verify(kitchenService).addToQueue(any(KitchenQueueDto.class));
    }

    @Test
    void addToQueue_WithInvalidData_ReturnsBadRequest() {
        // Given - Invalid DTO without required fields
        KitchenQueueDto invalidDto = new KitchenQueueDto();

        // When & Then
        webTestClient.post()
                .uri("/api/kitchen/queue")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidDto)
                .exchange()
                .expectStatus().isBadRequest();

        verify(kitchenService, never()).addToQueue(any());
    }

    @Test
    void getActiveQueue_ReturnsActiveItems() {
        // Given
        KitchenQueueDto dto1 = createMockKitchenQueueDto(1L);
        dto1.setStatus(DishStatus.PENDING);
        KitchenQueueDto dto2 = createMockKitchenQueueDto(2L);
        dto2.setStatus(DishStatus.IN_PROGRESS);

        when(kitchenService.getActiveQueue()).thenReturn(Flux.just(dto1, dto2));

        // When & Then
        webTestClient.get()
                .uri("/api/kitchen/queue")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(KitchenQueueDto.class)
                .hasSize(2);

        verify(kitchenService).getActiveQueue();
    }

    @Test
    void getAllQueue_ReturnsAllItems() {
        // Given
        when(kitchenService.getAllQueue()).thenReturn(Flux.just(
                createMockKitchenQueueDto(1L),
                createMockKitchenQueueDto(2L),
                createMockKitchenQueueDto(3L)
        ));

        // When & Then
        webTestClient.get()
                .uri("/api/kitchen/queue/all")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(KitchenQueueDto.class)
                .hasSize(3);

        verify(kitchenService).getAllQueue();
    }

    @Test
    void getQueueItemById_WhenExists_ReturnsItem() {
        // Given
        Long queueId = 1L;
        KitchenQueueDto dto = createMockKitchenQueueDto(queueId);

        when(kitchenService.getQueueItemById(queueId)).thenReturn(Mono.just(dto));

        // When & Then
        webTestClient.get()
                .uri("/api/kitchen/queue/{id}", queueId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.dishName").isEqualTo("Test Dish")
                .jsonPath("$.status").isEqualTo("PENDING");

        verify(kitchenService).getQueueItemById(queueId);
    }

    @Test
    void getQueueItemById_WhenNotExists_ReturnsNotFound() {
        // Given
        Long queueId = 999L;
        when(kitchenService.getQueueItemById(queueId))
                .thenReturn(Mono.error(new ResourceNotFoundException("Kitchen queue item not found with id: " + queueId)));

        // When & Then
        webTestClient.get()
                .uri("/api/kitchen/queue/{id}", queueId)
                .exchange()
                .expectStatus().isNotFound();

        verify(kitchenService).getQueueItemById(queueId);
    }

    @Test
    void updateStatus_UpdatesQueueItemStatus() {
        // Given
        Long queueId = 1L;
        DishStatus newStatus = DishStatus.IN_PROGRESS;
        KitchenQueueDto dto = createMockKitchenQueueDto(queueId);
        dto.setStatus(newStatus);

        when(kitchenService.updateStatus(queueId, newStatus)).thenReturn(Mono.just(dto));

        // When & Then
        webTestClient.put()
                .uri("/api/kitchen/queue/{id}/status?status={status}", queueId, newStatus)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.status").isEqualTo("IN_PROGRESS");

        verify(kitchenService).updateStatus(queueId, newStatus);
    }

    @Test
    void updateStatus_WhenItemNotFound_ReturnsNotFound() {
        // Given
        Long queueId = 999L;
        DishStatus newStatus = DishStatus.IN_PROGRESS;
        when(kitchenService.updateStatus(queueId, newStatus))
                .thenReturn(Mono.error(new ResourceNotFoundException("Kitchen queue item not found with id: " + queueId)));

        // When & Then
        webTestClient.put()
                .uri("/api/kitchen/queue/{id}/status?status={status}", queueId, newStatus)
                .exchange()
                .expectStatus().isNotFound();

        verify(kitchenService).updateStatus(queueId, newStatus);
    }

    @Test
    void getQueueByOrderId_ReturnsMatchingItems() {
        // Given
        Long orderId = 100L;
        KitchenQueueDto dto1 = createMockKitchenQueueDto(1L);
        KitchenQueueDto dto2 = createMockKitchenQueueDto(2L);

        when(kitchenService.getQueueByOrderId(orderId)).thenReturn(Flux.just(dto1, dto2));

        // When & Then
        webTestClient.get()
                .uri("/api/kitchen/queue/order/{orderId}", orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(KitchenQueueDto.class)
                .hasSize(2);

        verify(kitchenService).getQueueByOrderId(orderId);
    }

    @Test
    void getQueueByOrderId_WhenNoItems_ReturnsEmptyList() {
        // Given
        Long orderId = 999L;
        when(kitchenService.getQueueByOrderId(orderId)).thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/kitchen/queue/order/{orderId}", orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(KitchenQueueDto.class)
                .hasSize(0);

        verify(kitchenService).getQueueByOrderId(orderId);
    }


    @Test
    void updateStatus_WithReadyStatus_ReturnsUpdated() {
        // Given
        Long queueId = 1L;
        DishStatus newStatus = DishStatus.READY;
        KitchenQueueDto updatedDto = createMockKitchenQueueDto(queueId);
        updatedDto.setStatus(newStatus);

        when(kitchenService.updateStatus(queueId, newStatus)).thenReturn(Mono.just(updatedDto));

        // When & Then
        webTestClient.put()
                .uri("/api/kitchen/queue/{id}/status?status={status}", queueId, newStatus)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("READY");

        verify(kitchenService).updateStatus(queueId, newStatus);
    }

    @Test
    void updateStatus_WithServedStatus_ReturnsUpdated() {
        // Given
        Long queueId = 1L;
        DishStatus newStatus = DishStatus.SERVED;
        KitchenQueueDto updatedDto = createMockKitchenQueueDto(queueId);
        updatedDto.setStatus(newStatus);

        when(kitchenService.updateStatus(queueId, newStatus)).thenReturn(Mono.just(updatedDto));

        // When & Then
        webTestClient.put()
                .uri("/api/kitchen/queue/{id}/status?status={status}", queueId, newStatus)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("SERVED");

        verify(kitchenService).updateStatus(queueId, newStatus);
    }

    @Test
    void getActiveQueue_EmptyQueue_ReturnsEmptyList() {
        // Given
        when(kitchenService.getActiveQueue()).thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/api/kitchen/queue")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(KitchenQueueDto.class)
                .hasSize(0);

        verify(kitchenService).getActiveQueue();
    }

}
