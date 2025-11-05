package ru.ifmo.se.restaurant.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.dto.BillDto;
import ru.ifmo.se.restaurant.exception.BusinessException;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.model.entity.Bill;
import ru.ifmo.se.restaurant.model.entity.Order;
import ru.ifmo.se.restaurant.model.OrderStatus;
import ru.ifmo.se.restaurant.repository.BillRepository;
import ru.ifmo.se.restaurant.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BillingService {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    private final BillRepository billRepository;
    private final OrderRepository orderRepository;

    public BillingService(BillRepository billRepository,
                         OrderRepository orderRepository) {
        this.billRepository = billRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public BillDto finalizeOrder(Long orderId, BigDecimal discount, String notes) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.READY && order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("Order must be READY or DELIVERED to be finalized");
        }

        if (billRepository.findByOrderId(orderId).isPresent()) {
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

        Bill savedBill = billRepository.save(bill);
        order.setBill(savedBill);
        
        if (order.getStatus() != OrderStatus.DELIVERED) {
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }

        return toBillDto(savedBill);
    }

    public BillDto getBillByOrderId(Long orderId) {
        Bill bill = billRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found for order id: " + orderId));
        return toBillDto(bill);
    }

    private BillDto toBillDto(Bill bill) {
        BillDto dto = new BillDto();
        dto.setId(bill.getId());
        dto.setOrderId(bill.getOrder().getId());
        dto.setSubtotal(bill.getSubtotal());
        dto.setDiscount(bill.getDiscount());
        dto.setTax(bill.getTax());
        dto.setTotal(bill.getTotal());
        dto.setIssuedAt(bill.getIssuedAt());
        dto.setNotes(bill.getNotes());
        return dto;
    }
}

