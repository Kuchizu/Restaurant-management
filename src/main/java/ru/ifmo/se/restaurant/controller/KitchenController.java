package ru.ifmo.se.restaurant.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.model.DishStatus;
import ru.ifmo.se.restaurant.service.KitchenService;

import java.util.List;

@RestController
@RequestMapping("/api/kitchen")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Kitchen Management", description = "API for kitchen queue management")
public class KitchenController {
    private final KitchenService kitchenService;

    public KitchenController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }

    @GetMapping("/queue")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get kitchen queue")
    public ResponseEntity<List<KitchenQueueDto>> getKitchenQueue() {
        return ResponseEntity.ok(kitchenService.getKitchenQueue());
    }

    @GetMapping("/queue/all")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all kitchen queue items")
    public ResponseEntity<Page<KitchenQueueDto>> getAllKitchenQueueItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(kitchenService.getAllKitchenQueueItems(page, size));
    }

    @PatchMapping("/queue/{queueId}/status")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update dish status")
    public ResponseEntity<KitchenQueueDto> updateDishStatus(
            @PathVariable Long queueId,
            @RequestParam DishStatus status) {
        return ResponseEntity.ok(kitchenService.updateDishStatus(queueId, status));
    }
}

