package ru.ifmo.se.restaurant.billing.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.billing.application.dto.BillDto;
import ru.ifmo.se.restaurant.billing.application.dto.OrderDto;
import ru.ifmo.se.restaurant.billing.application.port.out.BillingEventPublisher;
import ru.ifmo.se.restaurant.billing.application.port.out.BillRepository;
import ru.ifmo.se.restaurant.billing.application.port.out.OrderServicePort;
import ru.ifmo.se.restaurant.billing.domain.entity.Bill;
import ru.ifmo.se.restaurant.billing.domain.exception.BillNotFoundException;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;
import ru.ifmo.se.restaurant.billing.domain.valueobject.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import ru.ifmo.se.restaurant.billing.domain.exception.BillAlreadyExistsException;
import ru.ifmo.se.restaurant.billing.domain.exception.InvalidBillOperationException;
import ru.ifmo.se.restaurant.billing.domain.exception.OrderServiceException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock private BillRepository billRepository;
    @Mock private OrderServicePort orderServicePort;
    @Mock private BillingEventPublisher billingEventPublisher;
    @InjectMocks private BillingService billingService;

    private Bill testBill;

    @BeforeEach
    void setUp() {
        testBill = Bill.builder()
                .id(1L).orderId(100L)
                .totalAmount(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .serviceCharge(new BigDecimal("5.00"))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("115.00"))
                .status(BillStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllBills_ShouldReturnList() {
        when(billRepository.findAll()).thenReturn(List.of(testBill));
        List<BillDto> result = billingService.getAllBills();
        assertEquals(1, result.size());
    }

    @Test
    void getBillById_ShouldReturnBill() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(testBill));
        BillDto result = billingService.getBillById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getBillById_ShouldThrowException_WhenNotFound() {
        when(billRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(BillNotFoundException.class, () -> billingService.getBillById(999L));
    }

    @Test
    void generateBill_ShouldCreateBill() {
        OrderDto order = new OrderDto();
        order.setId(100L);
        order.setTotalAmount(new BigDecimal("100.00"));
        when(orderServicePort.getOrder(100L)).thenReturn(order);
        when(billRepository.findByOrderId(100L)).thenReturn(Optional.empty());
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);
        doNothing().when(billingEventPublisher).publishBillGenerated(any());

        BillDto result = billingService.generateBill(100L);
        assertNotNull(result);
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void payBill_ShouldUpdateStatus() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(testBill));
        Bill paidBill = Bill.builder().id(1L).orderId(100L).status(BillStatus.PAID).build();
        when(billRepository.save(any(Bill.class))).thenReturn(paidBill);
        doNothing().when(billingEventPublisher).publishBillPaid(any());

        BillDto result = billingService.payBill(1L, PaymentMethod.CASH);
        assertNotNull(result);
    }

    @Test
    void cancelBill_ShouldUpdateStatus() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(testBill));
        Bill cancelled = Bill.builder().id(1L).status(BillStatus.CANCELLED).build();
        when(billRepository.save(any(Bill.class))).thenReturn(cancelled);

        BillDto result = billingService.cancelBill(1L);
        assertNotNull(result);
    }

    @Test
    void deleteBill_ShouldDelete() {
        when(billRepository.existsById(1L)).thenReturn(true);
        doNothing().when(billRepository).deleteById(1L);
        assertDoesNotThrow(() -> billingService.deleteBill(1L));
    }

    @Test
    void deleteBill_ShouldThrow_WhenNotFound() {
        when(billRepository.existsById(999L)).thenReturn(false);
        assertThrows(BillNotFoundException.class, () -> billingService.deleteBill(999L));
    }

    @Test
    void getBillByOrderId_ShouldReturnBill() {
        when(billRepository.findByOrderId(100L)).thenReturn(Optional.of(testBill));
        BillDto result = billingService.getBillByOrderId(100L);
        assertEquals(100L, result.getOrderId());
    }

    @Test
    void getBillByOrderId_ShouldThrow_WhenNotFound() {
        when(billRepository.findByOrderId(999L)).thenReturn(Optional.empty());
        assertThrows(BillNotFoundException.class, () -> billingService.getBillByOrderId(999L));
    }

    @Test
    void getBillsByStatus_ShouldReturnList() {
        when(billRepository.findByStatus(BillStatus.PENDING)).thenReturn(List.of(testBill));
        List<BillDto> result = billingService.getBillsByStatus(BillStatus.PENDING);
        assertEquals(1, result.size());
    }

    @Test
    void getAllBillsPaginated_ShouldReturnPage() {
        Page<Bill> page = new PageImpl<>(List.of(testBill));
        when(billRepository.findAll(any(Pageable.class))).thenReturn(page);
        Page<BillDto> result = billingService.getAllBillsPaginated(0, 10);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAllBillsSlice_ShouldReturnSlice() {
        Slice<Bill> slice = new SliceImpl<>(List.of(testBill));
        when(billRepository.findAllSlice(any(Pageable.class))).thenReturn(slice);
        Slice<BillDto> result = billingService.getAllBillsSlice(0, 10);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getBillsByStatusPaginated_ShouldReturnPage() {
        Page<Bill> page = new PageImpl<>(List.of(testBill));
        when(billRepository.findByStatus(eq(BillStatus.PENDING), any(Pageable.class))).thenReturn(page);
        Page<BillDto> result = billingService.getBillsByStatusPaginated(BillStatus.PENDING, 0, 10);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void generateBill_ShouldThrow_WhenOrderNull() {
        when(orderServicePort.getOrder(100L)).thenReturn(null);
        assertThrows(OrderServiceException.class, () -> billingService.generateBill(100L));
    }

    @Test
    void generateBill_ShouldThrow_WhenBillExists() {
        OrderDto order = new OrderDto();
        order.setId(100L);
        when(orderServicePort.getOrder(100L)).thenReturn(order);
        when(billRepository.findByOrderId(100L)).thenReturn(Optional.of(testBill));
        assertThrows(BillAlreadyExistsException.class, () -> billingService.generateBill(100L));
    }

    @Test
    void generateBill_WithNullTotalAmount_ShouldUseZero() {
        OrderDto order = new OrderDto();
        order.setId(100L);
        order.setTotalAmount(null);
        when(orderServicePort.getOrder(100L)).thenReturn(order);
        when(billRepository.findByOrderId(100L)).thenReturn(Optional.empty());
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);
        doNothing().when(billingEventPublisher).publishBillGenerated(any());

        BillDto result = billingService.generateBill(100L);
        assertNotNull(result);
    }

    @Test
    void applyDiscount_ShouldUpdateBill() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(testBill));
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);
        BillDto result = billingService.applyDiscount(1L, new BigDecimal("10.00"));
        assertNotNull(result);
    }

    @Test
    void applyDiscount_ShouldThrow_WhenNotPending() {
        Bill paidBill = Bill.builder().id(1L).status(BillStatus.PAID).build();
        when(billRepository.findById(1L)).thenReturn(Optional.of(paidBill));
        assertThrows(InvalidBillOperationException.class,
            () -> billingService.applyDiscount(1L, new BigDecimal("10.00")));
    }

    @Test
    void applyDiscount_ShouldThrow_WhenNotFound() {
        when(billRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(BillNotFoundException.class,
            () -> billingService.applyDiscount(999L, new BigDecimal("10.00")));
    }

    @Test
    void payBill_ShouldThrow_WhenNotPending() {
        Bill paidBill = Bill.builder().id(1L).status(BillStatus.PAID).build();
        when(billRepository.findById(1L)).thenReturn(Optional.of(paidBill));
        assertThrows(InvalidBillOperationException.class,
            () -> billingService.payBill(1L, PaymentMethod.CASH));
    }

    @Test
    void payBill_ShouldThrow_WhenNotFound() {
        when(billRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(BillNotFoundException.class,
            () -> billingService.payBill(999L, PaymentMethod.CASH));
    }

    @Test
    void cancelBill_ShouldThrow_WhenPaid() {
        Bill paidBill = Bill.builder().id(1L).status(BillStatus.PAID).build();
        when(billRepository.findById(1L)).thenReturn(Optional.of(paidBill));
        assertThrows(InvalidBillOperationException.class, () -> billingService.cancelBill(1L));
    }

    @Test
    void cancelBill_ShouldThrow_WhenNotFound() {
        when(billRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(BillNotFoundException.class, () -> billingService.cancelBill(999L));
    }
}
