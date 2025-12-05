package ru.ifmo.se.restaurant.dataaccess;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.model.entity.Bill;
import ru.ifmo.se.restaurant.model.entity.Order;
import ru.ifmo.se.restaurant.repository.BillRepository;
import ru.ifmo.se.restaurant.repository.OrderRepository;

@Component
public class BillingDataAccess {
    private final BillRepository billRepository;
    private final OrderRepository orderRepository;
    public BillingDataAccess(BillRepository billRepository, OrderRepository orderRepository) {
        this.billRepository = billRepository;
        this.orderRepository = orderRepository;
    }
    public Order findOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }
    public Bill saveBill(Bill bill) {
        return billRepository.save(bill);
    }
    public java.util.Optional<Bill> findBillByOrderId(Long orderId) {
        return billRepository.findByOrderId(orderId);
    }
    public Bill findBillByOrderIdOrThrow(Long orderId) {
        return billRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found for order id: " + orderId));
    }
    public Bill findBillById(Long id) {
        return billRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
    }
    public Page<Bill> findAllBills(Pageable pageable) {
        return billRepository.findAll(pageable);
    }
    public void deleteBill(Bill bill) { billRepository.delete(bill); }
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
}
