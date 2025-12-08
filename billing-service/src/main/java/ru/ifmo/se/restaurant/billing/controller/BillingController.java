package ru.ifmo.se.restaurant.billing.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.billing.dto.BillDto;
import ru.ifmo.se.restaurant.billing.entity.BillStatus;
import ru.ifmo.se.restaurant.billing.entity.PaymentMethod;
import ru.ifmo.se.restaurant.billing.service.BillingService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillingController {
    private final BillingService billingService;

    @GetMapping
    public ResponseEntity<List<BillDto>> getAllBills() {
        return ResponseEntity.ok(billingService.getAllBills());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillDto> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getBillById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<BillDto> getBillByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(billingService.getBillByOrderId(orderId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BillDto>> getBillsByStatus(@PathVariable BillStatus status) {
        return ResponseEntity.ok(billingService.getBillsByStatus(status));
    }

    @PostMapping("/generate/{orderId}")
    public ResponseEntity<BillDto> generateBill(@PathVariable Long orderId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingService.generateBill(orderId));
    }

    @PatchMapping("/{id}/discount")
    public ResponseEntity<BillDto> applyDiscount(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(billingService.applyDiscount(id, amount));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<BillDto> payBill(
            @PathVariable Long id,
            @RequestParam PaymentMethod paymentMethod) {
        return ResponseEntity.ok(billingService.payBill(id, paymentMethod));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BillDto> cancelBill(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.cancelBill(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(@PathVariable Long id) {
        billingService.deleteBill(id);
        return ResponseEntity.noContent().build();
    }
}
