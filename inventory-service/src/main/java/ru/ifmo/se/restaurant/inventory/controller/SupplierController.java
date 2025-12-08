package ru.ifmo.se.restaurant.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.inventory.dto.SupplierDto;
import ru.ifmo.se.restaurant.inventory.dto.SupplyOrderDto;
import ru.ifmo.se.restaurant.inventory.entity.SupplyOrderStatus;
import ru.ifmo.se.restaurant.inventory.service.SupplierService;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<List<SupplierDto>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDto> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PostMapping
    public ResponseEntity<SupplierDto> createSupplier(@RequestBody SupplierDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierService.createSupplier(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDto> updateSupplier(
            @PathVariable Long id,
            @RequestBody SupplierDto dto) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public ResponseEntity<List<SupplyOrderDto>> getAllSupplyOrders() {
        return ResponseEntity.ok(supplierService.getAllSupplyOrders());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<SupplyOrderDto> getSupplyOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplyOrderById(id));
    }

    @GetMapping("/orders/status/{status}")
    public ResponseEntity<List<SupplyOrderDto>> getSupplyOrdersByStatus(
            @PathVariable SupplyOrderStatus status) {
        return ResponseEntity.ok(supplierService.getSupplyOrdersByStatus(status));
    }

    @PostMapping("/orders")
    public ResponseEntity<SupplyOrderDto> createSupplyOrder(@RequestBody SupplyOrderDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierService.createSupplyOrder(dto));
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<SupplyOrderDto> updateSupplyOrderStatus(
            @PathVariable Long id,
            @RequestParam SupplyOrderStatus status) {
        return ResponseEntity.ok(supplierService.updateSupplyOrderStatus(id, status));
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteSupplyOrder(@PathVariable Long id) {
        supplierService.deleteSupplyOrder(id);
        return ResponseEntity.noContent().build();
    }
}
