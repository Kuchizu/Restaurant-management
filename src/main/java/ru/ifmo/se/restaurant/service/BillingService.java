package ru.ifmo.se.restaurant.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;
import ru.ifmo.se.restaurant.dto.BillDto;
import ru.ifmo.se.restaurant.exception.BusinessException;
import ru.ifmo.se.restaurant.model.entity.Bill;
import ru.ifmo.se.restaurant.model.entity.Order;
import ru.ifmo.se.restaurant.model.OrderStatus;
import ru.ifmo.se.restaurant.dataaccess.BillingDataAccess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BillingService {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    private final BillingDataAccess dataAccess;

    public BillingService(BillingDataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Transactional
    public BillDto finalizeOrder(@NonNull Long orderId, BigDecimal discount, String notes) {
        Order order = dataAccess.findOrderById(orderId);

        if (order.getStatus() != OrderStatus.READY && order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("Order must be READY or DELIVERED to be finalized");
        }

        if (dataAccess.findBillByOrderId(orderId).isPresent()) {
            throw new BusinessException("Bill already exists for this order");
        }

        if (discount == null) {
            discount = BigDecimal.ZERO;
        }
        
        if (discount.compareTo(order.getTotalAmount()) > 0) {
            throw new BusinessException("Discount cannot exceed order total");
        }

        BigDecimal subtotal = order.getTotalAmount().subtract(discount);
        BigDecimal tax = subtotal.multiply(TAX_RATE);
        BigDecimal total = subtotal.add(tax);

        Bill bill = new Bill();
        bill.setOrder(order);
        bill.setSubtotal(subtotal);
        bill.setDiscount(discount);
        bill.setTax(tax);
        bill.setTotal(total);
        bill.setIssuedAt(LocalDateTime.now());
        bill.setNotes(notes);

        Bill savedBill = dataAccess.saveBill(bill);
        order.setBill(savedBill);
        
        if (order.getStatus() != OrderStatus.DELIVERED) {
            order.setStatus(OrderStatus.DELIVERED);
            dataAccess.saveOrder(order);
        }

        return toBillDto(savedBill);
    }

    public BillDto getBillByOrderId(@NonNull Long orderId) {
        Bill bill = dataAccess.findBillByOrderIdOrThrow(orderId);
        return toBillDto(bill);
    }

    public BillDto getBillById(@NonNull Long id) {
        Bill bill = dataAccess.findBillById(id);
        return toBillDto(bill);
    }

    public Page<BillDto> getAllBills(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return dataAccess.findAllBills(pageable).map(this::toBillDto);
    }

    @Transactional
    public BillDto updateBill(@NonNull Long id, @NonNull BillDto dto) {
        Bill bill = dataAccess.findBillById(id);
        
        Order order = bill.getOrder();
        BigDecimal orderTotal = order.getTotalAmount();
        
        if (dto.getDiscount() != null) {
            if (dto.getDiscount().compareTo(orderTotal) > 0) {
                throw new BusinessException("Discount cannot exceed order total");
            }
            if (dto.getDiscount().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Discount cannot be negative");
            }
            
            bill.setDiscount(dto.getDiscount());
            BigDecimal subtotal = orderTotal.subtract(dto.getDiscount());
            BigDecimal tax = subtotal.multiply(TAX_RATE);
            BigDecimal total = subtotal.add(tax);
            
            bill.setSubtotal(subtotal);
            bill.setTax(tax);
            bill.setTotal(total);
        }
        
        if (dto.getNotes() != null) {
            bill.setNotes(dto.getNotes());
        }
        
        return toBillDto(dataAccess.saveBill(bill));
    }

    @Transactional
    public void deleteBill(@NonNull Long id) {
        Bill bill = dataAccess.findBillById(id);
        dataAccess.deleteBill(bill);
    }

    private BillDto toBillDto(Bill bill) {
        BillDto dto = new BillDto();
        dto.setId(bill.getId());
        Order order = bill.getOrder();
        dto.setOrderId(order.getId());
        dto.setSubtotal(bill.getSubtotal());
        dto.setDiscount(bill.getDiscount());
        dto.setTax(bill.getTax());
        dto.setTotal(bill.getTotal());
        dto.setIssuedAt(bill.getIssuedAt());
        dto.setNotes(bill.getNotes());
        return dto;
    }
}

