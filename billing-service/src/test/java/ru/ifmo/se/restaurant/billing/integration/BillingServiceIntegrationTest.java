package ru.ifmo.se.restaurant.billing.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.ifmo.se.restaurant.billing.client.OrderServiceClient;
import ru.ifmo.se.restaurant.billing.dto.BillDto;
import ru.ifmo.se.restaurant.billing.dto.OrderDto;
import ru.ifmo.se.restaurant.billing.entity.BillStatus;
import ru.ifmo.se.restaurant.billing.entity.PaymentMethod;
import ru.ifmo.se.restaurant.billing.exception.BusinessConflictException;
import ru.ifmo.se.restaurant.billing.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.billing.exception.ServiceUnavailableException;
import ru.ifmo.se.restaurant.billing.repository.BillRepository;
import ru.ifmo.se.restaurant.billing.service.BillingService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static ru.ifmo.se.restaurant.billing.TestDataFactory.createMockOrderDto;

@SpringBootTest
@ActiveProfiles("test")
class BillingServiceIntegrationTest {

    @Autowired
    private BillingService billingService;

    @Autowired
    private BillRepository billRepository;

    @MockBean
    private OrderServiceClient orderServiceClient;

    @BeforeEach
    void setUp() {
        billRepository.deleteAll();
    }

    @Test
    void generateBill_EndToEnd_CreatesAndSavesBill() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        orderDto.setTotalAmount(new BigDecimal("200.00"));
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

        // When
        BillDto result = billingService.generateBill(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("200.00");
        assertThat(result.getTaxAmount()).isEqualByComparingTo("20.00");
        assertThat(result.getServiceCharge()).isEqualByComparingTo("10.00");
        assertThat(result.getFinalAmount()).isEqualByComparingTo("230.00");
        assertThat(result.getStatus()).isEqualTo(BillStatus.PENDING);

        // Verify persistence
        assertThat(billRepository.findById(result.getId())).isPresent();
    }

    @Test
    void generateBill_WhenBillAlreadyExists_ThrowsConflict() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        orderDto.setTotalAmount(new BigDecimal("100.00"));
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

        // Create first bill
        billingService.generateBill(orderId);

        // When & Then
        assertThatThrownBy(() -> billingService.generateBill(orderId))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Bill already exists for this order");
    }

    @Test
    void generateBill_WhenOrderServiceReturnsNull_ThrowsServiceUnavailable() {
        // Given
        Long orderId = 1L;
        when(orderServiceClient.getOrder(orderId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> billingService.generateBill(orderId))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("Order service is currently unavailable");
    }

    @Test
    void generateBill_WithZeroAmount_HandlesCorrectly() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        orderDto.setTotalAmount(BigDecimal.ZERO);
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

        // When
        BillDto result = billingService.generateBill(orderId);

        // Then
        assertThat(result.getTotalAmount()).isEqualByComparingTo("0.00");
        assertThat(result.getTaxAmount()).isEqualByComparingTo("0.00");
        assertThat(result.getServiceCharge()).isEqualByComparingTo("0.00");
        assertThat(result.getFinalAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void applyDiscount_EndToEnd_UpdatesBillWithDiscount() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        orderDto.setTotalAmount(new BigDecimal("100.00"));
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

        BillDto bill = billingService.generateBill(orderId);
        BigDecimal discountAmount = new BigDecimal("20.00");

        // When
        BillDto result = billingService.applyDiscount(bill.getId(), discountAmount);

        // Then
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("20.00");
        assertThat(result.getFinalAmount()).isEqualByComparingTo("95.00");

        // Verify persistence
        BillDto retrievedBill = billingService.getBillById(bill.getId());
        assertThat(retrievedBill.getDiscountAmount()).isEqualByComparingTo("20.00");
        assertThat(retrievedBill.getFinalAmount()).isEqualByComparingTo("95.00");
    }

    @Test
    void applyDiscount_WhenBillNotPending_ThrowsConflict() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

        BillDto bill = billingService.generateBill(orderId);
        billingService.payBill(bill.getId(), PaymentMethod.CASH);

        // When & Then
        assertThatThrownBy(() -> billingService.applyDiscount(bill.getId(), new BigDecimal("10.00")))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Cannot apply discount to non-pending bill");
    }

    @Test
    void payBill_EndToEnd_UpdatesStatusToPaid() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

        BillDto bill = billingService.generateBill(orderId);

        // When
        BillDto result = billingService.payBill(bill.getId(), PaymentMethod.CASH);

        // Then
        assertThat(result.getStatus()).isEqualTo(BillStatus.PAID);
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(result.getPaidAt()).isNotNull();

        // Verify persistence
        BillDto retrievedBill = billingService.getBillById(bill.getId());
        assertThat(retrievedBill.getStatus()).isEqualTo(BillStatus.PAID);
        assertThat(retrievedBill.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
    }

    @Test
    void payBill_WithDifferentPaymentMethods_HandlesAllMethods() {
        // Given
        PaymentMethod[] methods = {PaymentMethod.CASH, PaymentMethod.CREDIT_CARD, PaymentMethod.MOBILE_PAYMENT};

        for (int i = 0; i < methods.length; i++) {
            Long orderId = (long) (i + 1);
            OrderDto orderDto = createMockOrderDto();
            when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

            BillDto bill = billingService.generateBill(orderId);

            // When
            BillDto result = billingService.payBill(bill.getId(), methods[i]);

            // Then
            assertThat(result.getStatus()).isEqualTo(BillStatus.PAID);
            assertThat(result.getPaymentMethod()).isEqualTo(methods[i]);
        }
    }

    @Test
    void payBill_WhenAlreadyPaid_ThrowsConflict() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

        BillDto bill = billingService.generateBill(orderId);
        billingService.payBill(bill.getId(), PaymentMethod.CASH);

        // When & Then
        assertThatThrownBy(() -> billingService.payBill(bill.getId(), PaymentMethod.CREDIT_CARD))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Cannot pay bill: must be in PENDING status");
    }

    @Test
    void cancelBill_EndToEnd_UpdatesStatusToCancelled() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

        BillDto bill = billingService.generateBill(orderId);

        // When
        BillDto result = billingService.cancelBill(bill.getId());

        // Then
        assertThat(result.getStatus()).isEqualTo(BillStatus.CANCELLED);

        // Verify persistence
        BillDto retrievedBill = billingService.getBillById(bill.getId());
        assertThat(retrievedBill.getStatus()).isEqualTo(BillStatus.CANCELLED);
    }

    @Test
    void cancelBill_WhenBillPaid_ThrowsConflict() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

        BillDto bill = billingService.generateBill(orderId);
        billingService.payBill(bill.getId(), PaymentMethod.CASH);

        // When & Then
        assertThatThrownBy(() -> billingService.cancelBill(bill.getId()))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Cannot cancel paid bill");
    }

    @Test
    void getBillByOrderId_EndToEnd_RetrievesBill() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

        BillDto createdBill = billingService.generateBill(orderId);

        // When
        BillDto result = billingService.getBillByOrderId(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(createdBill.getId());
        assertThat(result.getOrderId()).isEqualTo(orderId);
    }

    @Test
    void getBillByOrderId_WhenNotExists_ThrowsNotFoundException() {
        // Given
        Long nonExistentOrderId = 999L;

        // When & Then
        assertThatThrownBy(() -> billingService.getBillByOrderId(nonExistentOrderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Bill not found for order");
    }

    @Test
    void getBillsByStatus_EndToEnd_ReturnsMatchingBills() {
        // Given
        for (int i = 1; i <= 3; i++) {
            Long orderId = (long) i;
            OrderDto orderDto = createMockOrderDto();
            when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);
            billingService.generateBill(orderId);
        }

        // Pay two bills
        List<BillDto> allBills = billingService.getAllBills();
        billingService.payBill(allBills.get(0).getId(), PaymentMethod.CASH);
        billingService.payBill(allBills.get(1).getId(), PaymentMethod.CREDIT_CARD);

        // When
        List<BillDto> pendingBills = billingService.getBillsByStatus(BillStatus.PENDING);
        List<BillDto> paidBills = billingService.getBillsByStatus(BillStatus.PAID);

        // Then
        assertThat(pendingBills).hasSize(1);
        assertThat(paidBills).hasSize(2);
        assertThat(pendingBills).allMatch(bill -> bill.getStatus() == BillStatus.PENDING);
        assertThat(paidBills).allMatch(bill -> bill.getStatus() == BillStatus.PAID);
    }

    @Test
    void deleteBill_EndToEnd_RemovesBill() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

        BillDto bill = billingService.generateBill(orderId);
        Long billId = bill.getId();

        // When
        billingService.deleteBill(billId);

        // Then
        assertThatThrownBy(() -> billingService.getBillById(billId))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThat(billRepository.findById(billId)).isEmpty();
    }

    @Test
    void deleteBill_WhenNotExists_ThrowsNotFoundException() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        assertThatThrownBy(() -> billingService.deleteBill(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Bill not found");
    }

    @Test
    void completeWorkflow_GenerateApplyDiscountPayBill() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        orderDto.setTotalAmount(new BigDecimal("200.00"));
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

        // When - Generate bill
        BillDto bill = billingService.generateBill(orderId);
        assertThat(bill.getFinalAmount()).isEqualByComparingTo("230.00");

        // When - Apply discount
        BillDto discountedBill = billingService.applyDiscount(bill.getId(), new BigDecimal("30.00"));
        assertThat(discountedBill.getDiscountAmount()).isEqualByComparingTo("30.00");
        assertThat(discountedBill.getFinalAmount()).isEqualByComparingTo("200.00");

        // When - Pay bill
        BillDto paidBill = billingService.payBill(bill.getId(), PaymentMethod.CREDIT_CARD);
        assertThat(paidBill.getStatus()).isEqualTo(BillStatus.PAID);
        assertThat(paidBill.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);

        // Then - Verify final state
        BillDto finalBill = billingService.getBillById(bill.getId());
        assertThat(finalBill.getStatus()).isEqualTo(BillStatus.PAID);
        assertThat(finalBill.getDiscountAmount()).isEqualByComparingTo("30.00");
        assertThat(finalBill.getFinalAmount()).isEqualByComparingTo("200.00");
        assertThat(finalBill.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
    }

    @Test
    void getAllBills_EndToEnd_ReturnsAllCreatedBills() {
        // Given
        for (int i = 1; i <= 5; i++) {
            Long orderId = (long) i;
            OrderDto orderDto = createMockOrderDto();
            when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);
            billingService.generateBill(orderId);
        }

        // When
        List<BillDto> result = billingService.getAllBills();

        // Then
        assertThat(result).hasSize(5);
        assertThat(result).extracting(BillDto::getOrderId)
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    void getBillById_WhenNotExists_ThrowsNotFoundException() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        assertThatThrownBy(() -> billingService.getBillById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Bill not found");
    }

    @Test
    void multipleDiscounts_OnSameBill_UpdatesCorrectly() {
        // Given
        Long orderId = 1L;
        OrderDto orderDto = createMockOrderDto();
        orderDto.setTotalAmount(new BigDecimal("100.00"));
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDto);

        BillDto bill = billingService.generateBill(orderId);
        assertThat(bill.getFinalAmount()).isEqualByComparingTo("115.00");

        // When - Apply first discount
        BillDto firstDiscount = billingService.applyDiscount(bill.getId(), new BigDecimal("10.00"));
        assertThat(firstDiscount.getFinalAmount()).isEqualByComparingTo("105.00");

        // When - Apply second discount
        BillDto secondDiscount = billingService.applyDiscount(bill.getId(), new BigDecimal("20.00"));
        assertThat(secondDiscount.getFinalAmount()).isEqualByComparingTo("95.00");
    }
}
