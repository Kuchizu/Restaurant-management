package ru.ifmo.se.restaurant.controller;

import org.springframework.http.ResponseEntity;
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

    @PostMapping("/orders/{orderId}/finalize")
    @io.swagger.v3.oas.annotations.Operation(summary = "Finalize order and create bill")
    public ResponseEntity<BillDto> finalizeOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) BigDecimal discount,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(billingService.finalizeOrder(orderId, discount, notes));
    }

    @GetMapping("/orders/{orderId}/bill")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get bill by order ID")
    public ResponseEntity<BillDto> getBillByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(billingService.getBillByOrderId(orderId));
    }
}

