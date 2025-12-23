package ru.ifmo.se.restaurant.billing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.billing.client.OrderServiceClient;
import ru.ifmo.se.restaurant.billing.dataaccess.BillDataAccess;
import ru.ifmo.se.restaurant.billing.dto.BillDto;
import ru.ifmo.se.restaurant.billing.dto.OrderDto;
import ru.ifmo.se.restaurant.billing.entity.Bill;
import ru.ifmo.se.restaurant.billing.entity.BillStatus;
import ru.ifmo.se.restaurant.billing.entity.PaymentMethod;
import ru.ifmo.se.restaurant.billing.exception.BusinessConflictException;
import ru.ifmo.se.restaurant.billing.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.billing.exception.ServiceUnavailableException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.billing.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BillDataAccess billDataAccess;

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private BillingService billingService;

    @Test
    void getAllBills_ReturnsAllBillDtos() {
        // Given
        List<Bill> bills = Arrays.asList(
                createMockBill(1L),
                createMockBill(2L)
        );
        when(billDataAccess.findAll()).thenReturn(bills);

        // When
        List<BillDto> result = billingService.getAllBills();

        // Then
        assertThat(result).hasSize(2);
        verify(billDataAccess).findAll();
    }

    @Test
    void getBillById_WhenExists_ReturnsBillDto() {
        // Given
        Long billId = 1L;
        Bill bill = createMockBill(billId);
        when(billDataAccess.getById(billId)).thenReturn(bill);

        // When
        BillDto result = billingService.getBillById(billId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(billId);
        assertThat(result.getStatus()).isEqualTo(BillStatus.PENDING);
        verify(billDataAccess).getById(billId);
    }

    @Test
    void getBillById_WhenNotExists_ThrowsException() {
        // Given
        Long billId = 999L;
        when(billDataAccess.getById(billId)).thenThrow(new ResourceNotFoundException("Bill not found"));

        // When & Then
        assertThatThrownBy(() -> billingService.getBillById(billId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(billDataAccess).getById(billId);
    }

    @Test
    void getBillByOrderId_WhenExists_ReturnsBillDto() {
        // Given
        Long orderId = 100L;
        Bill bill = createMockBill(1L);
        when(billDataAccess.findByOrderId(orderId)).thenReturn(Optional.of(bill));

        // When
        BillDto result = billingService.getBillByOrderId(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        verify(billDataAccess).findByOrderId(orderId);
    }

    @Test
    void getBillsByStatus_ReturnsMatchingBills() {
        // Given
        BillStatus status = BillStatus.PENDING;
        List<Bill> bills = Arrays.asList(createMockBill(1L), createMockBill(2L));
        when(billDataAccess.findByStatus(status)).thenReturn(bills);

        // When
        List<BillDto> result = billingService.getBillsByStatus(status);

        // Then
        assertThat(result).hasSize(2);
        verify(billDataAccess).findByStatus(status);
    }

    @Test
    void generateBill_WhenOrderExists_CreatesBill() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        orderDto.setTotalAmount(new BigDecimal("100.00"));

        when(billDataAccess.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);
        when(billDataAccess.save(any(Bill.class))).thenAnswer(inv -> {
            Bill bill = inv.getArgument(0);
            bill.setId(1L);
            return bill;
        });

        // When
        BillDto result = billingService.generateBill(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("100.00");
        assertThat(result.getTaxAmount()).isEqualByComparingTo("10.00");
        assertThat(result.getServiceCharge()).isEqualByComparingTo("5.00");
        assertThat(result.getFinalAmount()).isEqualByComparingTo("115.00");
        assertThat(result.getStatus()).isEqualTo(BillStatus.PENDING);

        verify(billDataAccess).findByOrderId(orderId);
        verify(orderServiceClient).getOrder(orderId);
        verify(billDataAccess).save(any(Bill.class));
    }

    @Test
    void generateBill_WhenBillAlreadyExists_ThrowsConflict() {
        // Given
        Long orderId = 1L;
        Bill existingBill = createMockBill(1L);
        OrderDto orderDto = new OrderDto();
        orderDto.setId(orderId);
        orderDto.setTotalAmount(new BigDecimal("100.00"));

        // Сначала вызывается order-service, потом проверяется существующий счёт
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);
        when(billDataAccess.findByOrderId(orderId)).thenReturn(Optional.of(existingBill));

        // When & Then
        assertThatThrownBy(() -> billingService.generateBill(orderId))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Bill already exists for this order");

        verify(orderServiceClient).getOrder(orderId);
        verify(billDataAccess).findByOrderId(orderId);
        verify(billDataAccess, never()).save(any());
    }

    @Test
    void generateBill_WhenOrderServiceUnavailable_ThrowsServiceUnavailable() {
        // Given
        Long orderId = 1L;
        // Order-service вызывается первым и возвращает null (недоступен)
        when(orderServiceClient.getOrder(orderId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> billingService.generateBill(orderId))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("Order service is currently unavailable");

        verify(orderServiceClient).getOrder(orderId);
        // findByOrderId НЕ вызывается, т.к. ошибка происходит раньше
        verify(billDataAccess, never()).findByOrderId(any());
        verify(billDataAccess, never()).save(any());
    }

    @Test
    void applyDiscount_WhenBillPending_AppliesDiscount() {
        // Given
        Long billId = 1L;
        Bill bill = createBillWithStatus(billId, BillStatus.PENDING);
        BigDecimal discountAmount = new BigDecimal("20.00");

        when(billDataAccess.getById(billId)).thenReturn(bill);
        when(billDataAccess.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        BillDto result = billingService.applyDiscount(billId, discountAmount);

        // Then
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("20.00");
        assertThat(result.getFinalAmount()).isEqualByComparingTo("95.00");

        verify(billDataAccess).getById(billId);
        verify(billDataAccess).save(argThat(b ->
                b.getDiscountAmount().compareTo(discountAmount) == 0
        ));
    }

    @Test
    void applyDiscount_WhenBillNotPending_ThrowsConflict() {
        // Given
        Long billId = 1L;
        Bill bill = createBillWithStatus(billId, BillStatus.PAID);
        when(billDataAccess.getById(billId)).thenReturn(bill);

        // When & Then
        assertThatThrownBy(() -> billingService.applyDiscount(billId, BigDecimal.TEN))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Cannot apply discount to non-pending bill");

        verify(billDataAccess).getById(billId);
        verify(billDataAccess, never()).save(any());
    }

    @Test
    void payBill_WhenPending_UpdatesStatusToPaid() {
        // Given
        Long billId = 1L;
        Bill bill = createBillWithStatus(billId, BillStatus.PENDING);

        when(billDataAccess.getById(billId)).thenReturn(bill);
        when(billDataAccess.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        BillDto result = billingService.payBill(billId, PaymentMethod.CASH);

        // Then
        assertThat(result.getStatus()).isEqualTo(BillStatus.PAID);
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(result.getPaidAt()).isNotNull();

        verify(billDataAccess).getById(billId);
        verify(billDataAccess).save(argThat(b ->
                b.getStatus() == BillStatus.PAID &&
                        b.getPaymentMethod() == PaymentMethod.CASH
        ));
    }

    @Test
    void payBill_WhenNotPending_ThrowsConflict() {
        // Given
        Long billId = 1L;
        Bill bill = createBillWithStatus(billId, BillStatus.PAID);
        when(billDataAccess.getById(billId)).thenReturn(bill);

        // When & Then
        assertThatThrownBy(() -> billingService.payBill(billId, PaymentMethod.CASH))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Cannot pay bill: must be in PENDING status");

        verify(billDataAccess).getById(billId);
        verify(billDataAccess, never()).save(any());
    }

    @Test
    void cancelBill_WhenNotPaid_CancelsBill() {
        // Given
        Long billId = 1L;
        Bill bill = createBillWithStatus(billId, BillStatus.PENDING);

        when(billDataAccess.getById(billId)).thenReturn(bill);
        when(billDataAccess.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        BillDto result = billingService.cancelBill(billId);

        // Then
        assertThat(result.getStatus()).isEqualTo(BillStatus.CANCELLED);

        verify(billDataAccess).getById(billId);
        verify(billDataAccess).save(argThat(b -> b.getStatus() == BillStatus.CANCELLED));
    }

    @Test
    void cancelBill_WhenPaid_ThrowsConflict() {
        // Given
        Long billId = 1L;
        Bill bill = createBillWithStatus(billId, BillStatus.PAID);
        when(billDataAccess.getById(billId)).thenReturn(bill);

        // When & Then
        assertThatThrownBy(() -> billingService.cancelBill(billId))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Cannot cancel paid bill");

        verify(billDataAccess).getById(billId);
        verify(billDataAccess, never()).save(any());
    }

    @Test
    void deleteBill_WhenExists_DeletesBill() {
        // Given
        Long billId = 1L;
        when(billDataAccess.existsById(billId)).thenReturn(true);
        doNothing().when(billDataAccess).deleteById(billId);

        // When
        billingService.deleteBill(billId);

        // Then
        verify(billDataAccess).existsById(billId);
        verify(billDataAccess).deleteById(billId);
    }

    @Test
    void deleteBill_WhenNotExists_ThrowsException() {
        // Given
        Long billId = 999L;
        when(billDataAccess.existsById(billId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> billingService.deleteBill(billId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Bill not found");

        verify(billDataAccess).existsById(billId);
        verify(billDataAccess, never()).deleteById(any());
    }
}
