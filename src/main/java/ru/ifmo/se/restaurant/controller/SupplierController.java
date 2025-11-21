package ru.ifmo.se.restaurant.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.dto.SupplierDto;
import ru.ifmo.se.restaurant.dto.SupplyOrderDto;
import ru.ifmo.se.restaurant.service.SupplierService;

@RestController
@RequestMapping("/api/suppliers")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Supplier Management", description = "API for managing suppliers and supply orders")
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new supplier")
    public ResponseEntity<SupplierDto> createSupplier(@Valid @RequestBody SupplierDto dto) {
        return new ResponseEntity<>(supplierService.createSupplier(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get supplier by ID")
    public ResponseEntity<SupplierDto> getSupplier(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all suppliers")
    public ResponseEntity<Page<SupplierDto>> getAllSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(supplierService.getAllSuppliers(page, size));
    }

    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update supplier")
    public ResponseEntity<SupplierDto> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierDto dto) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, dto));
    }

    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete supplier")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/supply-orders")
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new supply order")
    public ResponseEntity<SupplyOrderDto> createSupplyOrder(@Valid @RequestBody SupplyOrderDto dto) {
        return new ResponseEntity<>(supplierService.createSupplyOrder(dto), HttpStatus.CREATED);
    }

    @GetMapping("/supply-orders/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get supply order by ID")
    public ResponseEntity<SupplyOrderDto> getSupplyOrder(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplyOrderById(id));
    }

    @GetMapping("/supply-orders")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all supply orders")
    public ResponseEntity<Page<SupplyOrderDto>> getAllSupplyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(supplierService.getAllSupplyOrders(page, size));
    }

    @PostMapping("/supply-orders/{orderId}/receive")
    @io.swagger.v3.oas.annotations.Operation(summary = "Receive supply order")
    public ResponseEntity<SupplyOrderDto> receiveSupplyOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(supplierService.receiveSupplyOrder(orderId));
    }
}

