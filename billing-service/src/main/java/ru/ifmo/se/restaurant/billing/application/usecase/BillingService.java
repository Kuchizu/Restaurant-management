package ru.ifmo.se.restaurant.billing.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.billing.application.dto.BillDto;
import ru.ifmo.se.restaurant.billing.application.dto.OrderDto;
import ru.ifmo.se.restaurant.billing.application.port.in.DeleteBillUseCase;
import ru.ifmo.se.restaurant.billing.application.port.in.GenerateBillUseCase;
import ru.ifmo.se.restaurant.billing.application.port.in.GetBillUseCase;
import ru.ifmo.se.restaurant.billing.application.port.in.UpdateBillUseCase;
import ru.ifmo.se.restaurant.billing.application.port.out.BillingEventPublisher;
import ru.ifmo.se.restaurant.billing.application.port.out.BillRepository;
import ru.ifmo.se.restaurant.billing.application.port.out.OrderServicePort;
import ru.ifmo.se.restaurant.billing.domain.entity.Bill;
import ru.ifmo.se.restaurant.billing.domain.exception.BillAlreadyExistsException;
import ru.ifmo.se.restaurant.billing.domain.exception.BillNotFoundException;
import ru.ifmo.se.restaurant.billing.domain.exception.InvalidBillOperationException;
import ru.ifmo.se.restaurant.billing.domain.exception.OrderServiceException;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;
import ru.ifmo.se.restaurant.billing.domain.valueobject.PaymentMethod;
import ru.ifmo.se.restaurant.billing.infrastructure.util.PaginationUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BillingService implements GenerateBillUseCase, GetBillUseCase, UpdateBillUseCase, DeleteBillUseCase {
    private final BillRepository billRepository;
    private final OrderServicePort orderServicePort;
    private final BillingEventPublisher billingEventPublisher;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private static final BigDecimal SERVICE_CHARGE_RATE = new BigDecimal("0.05");

    @Override
    @Transactional(readOnly = true)
    public List<BillDto> getAllBills() {
        return billRepository.findAll().stream()
                .map(BillDto::fromDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BillDto getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new BillNotFoundException(id));
        return BillDto.fromDomain(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public BillDto getBillByOrderId(Long orderId) {
        return billRepository.findByOrderId(orderId)
                .map(BillDto::fromDomain)
                .orElseThrow(() -> new BillNotFoundException("Bill not found for order: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillDto> getBillsByStatus(BillStatus status) {
        return billRepository.findByStatus(status).stream()
                .map(BillDto::fromDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillDto> getAllBillsPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return billRepository.findAll(pageable)
                .map(BillDto::fromDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<BillDto> getAllBillsSlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return billRepository.findAllSlice(pageable)
                .map(BillDto::fromDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillDto> getBillsByStatusPaginated(BillStatus status, int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return billRepository.findByStatus(status, pageable)
                .map(BillDto::fromDomain);
    }

    @Override
    public BillDto generateBill(Long orderId) {
        // 1. Check if order exists
        OrderDto order = orderServicePort.getOrder(orderId);
        if (order == null) {
            throw new OrderServiceException("Order service is currently unavailable or order not found");
        }

        // 2. Check if bill already exists for this order
        billRepository.findByOrderId(orderId).ifPresent(existingBill -> {
            throw new BillAlreadyExistsException(orderId);
        });

        // 3. Calculate amounts
        BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal taxAmount = totalAmount.multiply(TAX_RATE);
        BigDecimal serviceCharge = totalAmount.multiply(SERVICE_CHARGE_RATE);
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = totalAmount.add(taxAmount).add(serviceCharge).subtract(discountAmount);

        // 4. Create bill
        Bill bill = Bill.builder()
                .orderId(orderId)
                .totalAmount(totalAmount)
                .taxAmount(taxAmount)
                .serviceCharge(serviceCharge)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .status(BillStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Bill savedBill = billRepository.save(bill);

        // 5. Publish event
        log.info("Publishing BILL_GENERATED event for order: {}, bill: {}", orderId, savedBill.getId());
        billingEventPublisher.publishBillGenerated(savedBill);

        return BillDto.fromDomain(savedBill);
    }

    @Override
    public BillDto applyDiscount(Long billId, BigDecimal discountAmount) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException(billId));

        if (!bill.canApplyDiscount()) {
            throw new InvalidBillOperationException(
                "Cannot apply discount to non-pending bill. Current status: " + bill.getStatus()
            );
        }

        BigDecimal newFinalAmount = bill.getTotalAmount()
                .add(bill.getTaxAmount())
                .add(bill.getServiceCharge())
                .subtract(discountAmount);

        Bill updatedBill = Bill.builder()
                .id(bill.getId())
                .orderId(bill.getOrderId())
                .totalAmount(bill.getTotalAmount())
                .taxAmount(bill.getTaxAmount())
                .serviceCharge(bill.getServiceCharge())
                .discountAmount(discountAmount)
                .finalAmount(newFinalAmount)
                .status(bill.getStatus())
                .paymentMethod(bill.getPaymentMethod())
                .createdAt(bill.getCreatedAt())
                .paidAt(bill.getPaidAt())
                .notes(bill.getNotes())
                .build();

        return BillDto.fromDomain(billRepository.save(updatedBill));
    }

    @Override
    public BillDto payBill(Long billId, PaymentMethod paymentMethod) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException(billId));

        if (!bill.canBePaid()) {
            throw new InvalidBillOperationException(
                "Cannot pay bill: must be in PENDING status. Current status: " + bill.getStatus()
            );
        }

        Bill paidBill = Bill.builder()
                .id(bill.getId())
                .orderId(bill.getOrderId())
                .totalAmount(bill.getTotalAmount())
                .taxAmount(bill.getTaxAmount())
                .serviceCharge(bill.getServiceCharge())
                .discountAmount(bill.getDiscountAmount())
                .finalAmount(bill.getFinalAmount())
                .status(BillStatus.PAID)
                .paymentMethod(paymentMethod)
                .createdAt(bill.getCreatedAt())
                .paidAt(LocalDateTime.now())
                .notes(bill.getNotes())
                .build();

        Bill savedBill = billRepository.save(paidBill);

        // Publish event
        log.info("Publishing BILL_PAID event for order: {}, bill: {}", savedBill.getOrderId(), savedBill.getId());
        billingEventPublisher.publishBillPaid(savedBill);

        return BillDto.fromDomain(savedBill);
    }

    @Override
    public BillDto cancelBill(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException(billId));

        if (!bill.canBeCancelled()) {
            throw new InvalidBillOperationException(
                "Cannot cancel paid bill"
            );
        }

        Bill cancelledBill = Bill.builder()
                .id(bill.getId())
                .orderId(bill.getOrderId())
                .totalAmount(bill.getTotalAmount())
                .taxAmount(bill.getTaxAmount())
                .serviceCharge(bill.getServiceCharge())
                .discountAmount(bill.getDiscountAmount())
                .finalAmount(bill.getFinalAmount())
                .status(BillStatus.CANCELLED)
                .paymentMethod(bill.getPaymentMethod())
                .createdAt(bill.getCreatedAt())
                .paidAt(bill.getPaidAt())
                .notes(bill.getNotes())
                .build();

        return BillDto.fromDomain(billRepository.save(cancelledBill));
    }

    @Override
    public void deleteBill(Long id) {
        if (!billRepository.existsById(id)) {
            throw new BillNotFoundException(id);
        }
        billRepository.deleteById(id);
    }
}
