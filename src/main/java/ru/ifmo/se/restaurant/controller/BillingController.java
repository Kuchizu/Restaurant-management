package ru.ifmo.se.restaurant.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.dto.BillDto;
import ru.ifmo.se.restaurant.service.BillingService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/billing")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Billing", description = "API for billing and finalizing orders")
public class BillingController {
    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new bill by finalizing order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<BillDto> createBill(
            @RequestParam @NonNull Long orderId,
            @RequestParam(required = false) BigDecimal discount,
            @RequestParam(required = false) String notes) {
        return new ResponseEntity<>(billingService.finalizeOrder(orderId, discount, notes), HttpStatus.CREATED);
    }

    @PostMapping("/orders/{orderId}/finalize")
    @io.swagger.v3.oas.annotations.Operation(summary = "Finalize order and create bill")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<BillDto> finalizeOrder(
            @PathVariable @NonNull Long orderId,
            @RequestParam(required = false) BigDecimal discount,
            @RequestParam(required = false) String notes) {
        return new ResponseEntity<>(billingService.finalizeOrder(orderId, discount, notes), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get bill by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<BillDto> getBill(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(billingService.getBillById(id));
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all bills")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Page<BillDto>> getAllBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<BillDto> result = billingService.getAllBills(page, size);
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(result.getTotalElements()))
            .body(result);
    }

    @GetMapping("/orders/{orderId}/bill")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get bill by order ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<BillDto> getBillByOrderId(@PathVariable @NonNull Long orderId) {
        return ResponseEntity.ok(billingService.getBillByOrderId(orderId));
    }

    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update bill")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<BillDto> updateBill(@PathVariable @NonNull Long id, @Valid @RequestBody BillDto dto) {
        return ResponseEntity.ok(billingService.updateBill(id, dto));
    }

    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete bill")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "No content"),
        @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    public ResponseEntity<Void> deleteBill(@PathVariable @NonNull Long id) {
        billingService.deleteBill(id);
        return ResponseEntity.noContent().build();
    }
}

