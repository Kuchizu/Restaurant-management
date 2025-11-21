package ru.ifmo.se.restaurant.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.dto.InventoryDto;
import ru.ifmo.se.restaurant.service.InventoryService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Inventory Management", description = "API for managing inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Add inventory")
    public ResponseEntity<InventoryDto> addInventory(@Valid @RequestBody InventoryDto dto) {
        return new ResponseEntity<>(inventoryService.addInventory(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get inventory by ID")
    public ResponseEntity<InventoryDto> getInventory(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all inventory items")
    public ResponseEntity<Page<InventoryDto>> getAllInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<InventoryDto> result = inventoryService.getAllInventory(page, size);
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(result.getTotalElements()))
            .body(result);
    }

    @GetMapping("/expiring")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get expiring inventory")
    public ResponseEntity<List<InventoryDto>> getExpiringInventory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(inventoryService.getExpiringInventory(date));
    }

    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update inventory")
    public ResponseEntity<InventoryDto> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryDto dto) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, dto));
    }

    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete inventory")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }
}

