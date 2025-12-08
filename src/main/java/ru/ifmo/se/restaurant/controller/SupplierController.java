package ru.ifmo.se.restaurant.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<SupplierDto> createSupplier(@Valid @RequestBody SupplierDto dto) {
        return new ResponseEntity<>(supplierService.createSupplier(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get supplier by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<SupplierDto> getSupplier(
            @Parameter(description = "Supplier ID", required = true, example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all suppliers")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Page<SupplierDto>> getAllSuppliers(
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(supplierService.getAllSuppliers(page, size));
    }

    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update supplier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<SupplierDto> updateSupplier(
            @Parameter(description = "Supplier ID", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody SupplierDto dto) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, dto));
    }

    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete supplier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "No content"),
        @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    public ResponseEntity<Void> deleteSupplier(
            @Parameter(description = "Supplier ID", required = true, example = "1") @PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/supply-orders")
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new supply order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<SupplyOrderDto> createSupplyOrder(@Valid @RequestBody SupplyOrderDto dto) {
        return new ResponseEntity<>(supplierService.createSupplyOrder(dto), HttpStatus.CREATED);
    }

    @GetMapping("/supply-orders/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get supply order by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<SupplyOrderDto> getSupplyOrder(
            @Parameter(description = "Supply order ID", required = true, example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplyOrderById(id));
    }

    @GetMapping("/supply-orders")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all supply orders")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Page<SupplyOrderDto>> getAllSupplyOrders(
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(supplierService.getAllSupplyOrders(page, size));
    }
}
