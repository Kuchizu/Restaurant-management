package ru.ifmo.se.restaurant.billing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.billing.client.OrderServiceClient;
import ru.ifmo.se.restaurant.billing.dto.BillDto;
import ru.ifmo.se.restaurant.billing.dto.OrderDto;
import ru.ifmo.se.restaurant.billing.entity.Bill;
import ru.ifmo.se.restaurant.billing.entity.BillStatus;
import ru.ifmo.se.restaurant.billing.entity.PaymentMethod;
import ru.ifmo.se.restaurant.billing.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.billing.repository.BillRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {
    private final BillRepository billRepository;
    private final OrderServiceClient orderServiceClient;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private static final BigDecimal SERVICE_CHARGE_RATE = new BigDecimal("0.05");

    public List<BillDto> getAllBills() {
        return billRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public BillDto getBillById(Long id) {
        return billRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
    }

    public BillDto getBillByOrderId(Long orderId) {
        return billRepository.findByOrderId(orderId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found for order"));
    }

    public List<BillDto> getBillsByStatus(BillStatus status) {
        return billRepository.findByStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BillDto generateBill(Long orderId) {
        billRepository.findByOrderId(orderId).ifPresent(existingBill -> {
            throw new RuntimeException("Bill already exists for this order");
        });

        OrderDto order = orderServiceClient.getOrder(orderId);
        if (order == null) {
            throw new RuntimeException("Order service unavailable or order not found");
        }

        Bill bill = new Bill();
        bill.setOrderId(orderId);
        bill.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);

        BigDecimal taxAmount = bill.getTotalAmount().multiply(TAX_RATE);
        bill.setTaxAmount(taxAmount);

        BigDecimal serviceCharge = bill.getTotalAmount().multiply(SERVICE_CHARGE_RATE);
        bill.setServiceCharge(serviceCharge);

        bill.setDiscountAmount(BigDecimal.ZERO);

        BigDecimal finalAmount = bill.getTotalAmount()
                .add(taxAmount)
                .add(serviceCharge)
                .subtract(bill.getDiscountAmount());
        bill.setFinalAmount(finalAmount);

        bill.setStatus(BillStatus.PENDING);
        bill.setCreatedAt(LocalDateTime.now());

        return toDto(billRepository.save(bill));
    }

    @Transactional
    public BillDto applyDiscount(Long billId, BigDecimal discountAmount) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (bill.getStatus() != BillStatus.PENDING) {
            throw new RuntimeException("Cannot apply discount to non-pending bill");
        }

        bill.setDiscountAmount(discountAmount);
        bill.setFinalAmount(bill.getTotalAmount()
                .add(bill.getTaxAmount())
                .add(bill.getServiceCharge())
                .subtract(discountAmount));

        return toDto(billRepository.save(bill));
    }

    @Transactional
    public BillDto payBill(Long billId, PaymentMethod paymentMethod) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (bill.getStatus() != BillStatus.PENDING) {
            throw new RuntimeException("Bill is not in pending status");
        }

        bill.setStatus(BillStatus.PAID);
        bill.setPaymentMethod(paymentMethod);
        bill.setPaidAt(LocalDateTime.now());

        return toDto(billRepository.save(bill));
    }

    @Transactional
    public BillDto cancelBill(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (bill.getStatus() == BillStatus.PAID) {
            throw new RuntimeException("Cannot cancel paid bill");
        }

        bill.setStatus(BillStatus.CANCELLED);
        return toDto(billRepository.save(bill));
    }

    @Transactional
    public void deleteBill(Long id) {
        if (!billRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bill not found");
        }
        billRepository.deleteById(id);
    }

    private BillDto toDto(Bill bill) {
        BillDto dto = new BillDto();
        dto.setId(bill.getId());
        dto.setOrderId(bill.getOrderId());
        dto.setTotalAmount(bill.getTotalAmount());
        dto.setTaxAmount(bill.getTaxAmount());
        dto.setServiceCharge(bill.getServiceCharge());
        dto.setDiscountAmount(bill.getDiscountAmount());
        dto.setFinalAmount(bill.getFinalAmount());
        dto.setStatus(bill.getStatus());
        dto.setPaymentMethod(bill.getPaymentMethod());
        dto.setCreatedAt(bill.getCreatedAt());
        dto.setPaidAt(bill.getPaidAt());
        dto.setNotes(bill.getNotes());
        return dto;
    }
}
