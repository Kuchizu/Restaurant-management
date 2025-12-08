package ru.ifmo.se.restaurant.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.inventory.dto.IngredientDto;
import ru.ifmo.se.restaurant.inventory.dto.InventoryDto;
import ru.ifmo.se.restaurant.inventory.service.InventoryService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryDto>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDto> getInventoryById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryDto>> getLowStockInventory() {
        return ResponseEntity.ok(inventoryService.getLowStockInventory());
    }

    @PostMapping
    public ResponseEntity<InventoryDto> createInventory(@RequestBody InventoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createInventory(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDto> updateInventory(
            @PathVariable Long id,
            @RequestBody InventoryDto dto) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, dto));
    }

    @PatchMapping("/{id}/adjust")
    public ResponseEntity<InventoryDto> adjustInventory(
            @PathVariable Long id,
            @RequestParam BigDecimal quantity) {
        return ResponseEntity.ok(inventoryService.adjustInventory(id, quantity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ingredients")
    public ResponseEntity<List<IngredientDto>> getAllIngredients() {
        return ResponseEntity.ok(inventoryService.getAllIngredients());
    }

    @GetMapping("/ingredients/{id}")
    public ResponseEntity<IngredientDto> getIngredientById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getIngredientById(id));
    }

    @PostMapping("/ingredients")
    public ResponseEntity<IngredientDto> createIngredient(@RequestBody IngredientDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createIngredient(dto));
    }

    @PutMapping("/ingredients/{id}")
    public ResponseEntity<IngredientDto> updateIngredient(
            @PathVariable Long id,
            @RequestBody IngredientDto dto) {
        return ResponseEntity.ok(inventoryService.updateIngredient(id, dto));
    }

    @DeleteMapping("/ingredients/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        inventoryService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }
}
