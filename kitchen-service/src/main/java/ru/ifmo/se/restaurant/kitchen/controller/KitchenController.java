package ru.ifmo.se.restaurant.kitchen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.kitchen.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.service.KitchenService;

@RestController
@RequestMapping("/api/kitchen")
@RequiredArgsConstructor
public class KitchenController {
    private final KitchenService kitchenService;

    @PostMapping("/queue")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<KitchenQueueDto> addToQueue(@Valid @RequestBody KitchenQueueDto dto) {
        return kitchenService.addToQueue(dto);
    }

    @GetMapping("/queue")
    public Flux<KitchenQueueDto> getActiveQueue() {
        return kitchenService.getActiveQueue();
    }

    @GetMapping("/queue/all")
    public Flux<KitchenQueueDto> getAllQueue() {
        return kitchenService.getAllQueue();
    }

    @GetMapping("/queue/{id}")
    public Mono<KitchenQueueDto> getQueueItemById(@PathVariable Long id) {
        return kitchenService.getQueueItemById(id);
    }

    @PutMapping("/queue/{id}/status")
    public Mono<KitchenQueueDto> updateStatus(
            @PathVariable Long id,
            @RequestParam DishStatus status) {
        return kitchenService.updateStatus(id, status);
    }

    @GetMapping("/queue/order/{orderId}")
    public Flux<KitchenQueueDto> getQueueByOrderId(@PathVariable Long orderId) {
        return kitchenService.getQueueByOrderId(orderId);
    }
}
