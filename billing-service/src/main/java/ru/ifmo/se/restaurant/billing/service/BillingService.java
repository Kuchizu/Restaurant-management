package ru.ifmo.se.restaurant.billing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.billing.client.OrderServiceClient;
import ru.ifmo.se.restaurant.billing.dto.BillDto;
import ru.ifmo.se.restaurant.billing.dto.OrderDto;
import ru.ifmo.se.restaurant.billing.entity.Bill;
import ru.ifmo.se.restaurant.billing.entity.BillStatus;
import ru.ifmo.se.restaurant.billing.entity.PaymentMethod;
import ru.ifmo.se.restaurant.billing.dataaccess.BillDataAccess;
import ru.ifmo.se.restaurant.billing.exception.BusinessConflictException;
import ru.ifmo.se.restaurant.billing.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.billing.exception.ServiceUnavailableException;
import ru.ifmo.se.restaurant.billing.util.PaginationUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {
    private final BillDataAccess billDataAccess;
    private final OrderServiceClient orderServiceClient;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private static final BigDecimal SERVICE_CHARGE_RATE = new BigDecimal("0.05");

    public List<BillDto> getAllBills() {
        return billDataAccess.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public BillDto getBillById(Long id) {
        Bill bill = billDataAccess.getById(id);
        return toDto(bill);
    }

    public BillDto getBillByOrderId(Long orderId) {
        return billDataAccess.findByOrderId(orderId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found for order"));
    }

    public List<BillDto> getBillsByStatus(BillStatus status) {
        return billDataAccess.findByStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Page<BillDto> getAllBillsPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return billDataAccess.findAll(pageable)
                .map(this::toDto);
    }

    public Slice<BillDto> getAllBillsSlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return billDataAccess.findAllSlice(pageable)
                .map(this::toDto);
    }

    public Page<BillDto> getBillsByStatusPaginated(BillStatus status, int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return billDataAccess.findByStatus(status, pageable)
                .map(this::toDto);
    }

    @Transactional
    public BillDto generateBill(Long orderId) {
        billDataAccess.findByOrderId(orderId).ifPresent(existingBill -> {
            throw new BusinessConflictException(
                "Bill already exists for this order",
                "Bill",
                orderId,
                "Existing bill ID: " + existingBill.getId()
            );
        });

        OrderDto order = orderServiceClient.getOrder(orderId);
        if (order == null) {
            throw new ServiceUnavailableException(
                "Order service is currently unavailable",
                "order-service",
                "getOrder"
            );
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

        return toDto(billDataAccess.save(bill));
    }

    @Transactional
    public BillDto applyDiscount(Long billId, BigDecimal discountAmount) {
        Bill bill = billDataAccess.getById(billId);

        if (bill.getStatus() != BillStatus.PENDING) {
            throw new BusinessConflictException(
                "Cannot apply discount to non-pending bill",
                "Bill",
                billId,
                "Current status: " + bill.getStatus()
            );
        }

        bill.setDiscountAmount(discountAmount);
        bill.setFinalAmount(bill.getTotalAmount()
                .add(bill.getTaxAmount())
                .add(bill.getServiceCharge())
                .subtract(discountAmount));

        return toDto(billDataAccess.save(bill));
    }

    @Transactional
    public BillDto payBill(Long billId, PaymentMethod paymentMethod) {
        Bill bill = billDataAccess.getById(billId);

        if (bill.getStatus() != BillStatus.PENDING) {
            throw new BusinessConflictException(
                "Cannot pay bill: must be in PENDING status",
                "Bill",
                billId,
                "Current status: " + bill.getStatus()
            );
        }

        bill.setStatus(BillStatus.PAID);
        bill.setPaymentMethod(paymentMethod);
        bill.setPaidAt(LocalDateTime.now());

        return toDto(billDataAccess.save(bill));
    }

    @Transactional
    public BillDto cancelBill(Long billId) {
        Bill bill = billDataAccess.getById(billId);

        if (bill.getStatus() == BillStatus.PAID) {
            throw new BusinessConflictException(
                "Cannot cancel paid bill",
                "Bill",
                billId,
                "Bill has already been paid"
            );
        }

        bill.setStatus(BillStatus.CANCELLED);
        return toDto(billDataAccess.save(bill));
    }

    @Transactional
    public void deleteBill(Long id) {
        if (!billDataAccess.existsById(id)) {
            throw new ResourceNotFoundException("Bill not found");
        }
        billDataAccess.deleteById(id);
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
