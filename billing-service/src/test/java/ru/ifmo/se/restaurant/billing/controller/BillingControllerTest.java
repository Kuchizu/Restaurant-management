package ru.ifmo.se.restaurant.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.ifmo.se.restaurant.billing.dto.BillDto;
import ru.ifmo.se.restaurant.billing.entity.BillStatus;
import ru.ifmo.se.restaurant.billing.entity.PaymentMethod;
import ru.ifmo.se.restaurant.billing.exception.BusinessConflictException;
import ru.ifmo.se.restaurant.billing.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.billing.service.BillingService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.ifmo.se.restaurant.billing.TestDataFactory.createMockBillDto;

@WebMvcTest(BillingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillingService billingService;

    @Test
    void getAllBills_ReturnsListOfBills() throws Exception {
        // Given
        List<BillDto> bills = Arrays.asList(
                createMockBillDto(1L),
                createMockBillDto(2L)
        );
        when(billingService.getAllBills()).thenReturn(bills);

        // When & Then
        mockMvc.perform(get("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(billingService).getAllBills();
    }

    @Test
    void getAllBills_WhenEmpty_ReturnsEmptyList() throws Exception {
        // Given
        when(billingService.getAllBills()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(billingService).getAllBills();
    }

    @Test
    void getAllBillsPaged_ReturnsPagedBills() throws Exception {
        // Given
        List<BillDto> bills = Arrays.asList(createMockBillDto(1L));
        Page<BillDto> page = new PageImpl<>(bills);
        when(billingService.getAllBillsPaginated(eq(0), eq(20)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/bills/paged?page=0&size=20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(header().exists("X-Total-Pages"))
                .andExpect(header().exists("X-Page-Number"))
                .andExpect(header().exists("X-Page-Size"));

        verify(billingService).getAllBillsPaginated(eq(0), eq(20));
    }

    @Test
    void getAllBillsInfiniteScroll_ReturnsSlicedBills() throws Exception {
        // Given
        List<BillDto> bills = Arrays.asList(createMockBillDto(1L));
        Slice<BillDto> slice = new SliceImpl<>(bills);
        when(billingService.getAllBillsSlice(eq(0), eq(20)))
                .thenReturn(slice);

        // When & Then
        mockMvc.perform(get("/api/bills/infinite-scroll?page=0&size=20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Has-Next"))
                .andExpect(header().exists("X-Has-Previous"))
                .andExpect(header().exists("X-Page-Number"))
                .andExpect(header().exists("X-Page-Size"));

        verify(billingService).getAllBillsSlice(eq(0), eq(20));
    }

    @Test
    void getBillById_WhenExists_ReturnsBill() throws Exception {
        // Given
        Long billId = 1L;
        BillDto billDto = createMockBillDto(billId);
        when(billingService.getBillById(billId)).thenReturn(billDto);

        // When & Then
        mockMvc.perform(get("/api/bills/{id}", billId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(billId.intValue())))
                .andExpect(jsonPath("$.orderId", is(100)))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalAmount", is(100.00)))
                .andExpect(jsonPath("$.finalAmount", is(115.00)));

        verify(billingService).getBillById(billId);
    }

    @Test
    void getBillById_WhenNotExists_ReturnsNotFound() throws Exception {
        // Given
        Long billId = 999L;
        when(billingService.getBillById(billId))
                .thenThrow(new ResourceNotFoundException("Bill not found"));

        // When & Then
        mockMvc.perform(get("/api/bills/{id}", billId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(billingService).getBillById(billId);
    }

    @Test
    void getBillByOrderId_WhenExists_ReturnsBill() throws Exception {
        // Given
        Long orderId = 100L;
        BillDto billDto = createMockBillDto(1L);
        when(billingService.getBillByOrderId(orderId)).thenReturn(billDto);

        // When & Then
        mockMvc.perform(get("/api/bills/order/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(orderId.intValue())))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(billingService).getBillByOrderId(orderId);
    }

    @Test
    void getBillByOrderId_WhenNotExists_ReturnsNotFound() throws Exception {
        // Given
        Long orderId = 999L;
        when(billingService.getBillByOrderId(orderId))
                .thenThrow(new ResourceNotFoundException("Bill not found for order"));

        // When & Then
        mockMvc.perform(get("/api/bills/order/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(billingService).getBillByOrderId(orderId);
    }

    @Test
    void getBillsByStatus_ReturnsBillsWithStatus() throws Exception {
        // Given
        BillStatus status = BillStatus.PENDING;
        List<BillDto> bills = Arrays.asList(
                createMockBillDto(1L),
                createMockBillDto(2L)
        );
        when(billingService.getBillsByStatus(status)).thenReturn(bills);

        // When & Then
        mockMvc.perform(get("/api/bills/status/{status}", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andExpect(jsonPath("$[1].status", is("PENDING")));

        verify(billingService).getBillsByStatus(status);
    }

    @Test
    void getBillsByStatus_WhenNoneFound_ReturnsEmptyList() throws Exception {
        // Given
        BillStatus status = BillStatus.CANCELLED;
        when(billingService.getBillsByStatus(status)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/bills/status/{status}", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(billingService).getBillsByStatus(status);
    }

    @Test
    void generateBill_WhenSuccessful_ReturnsCreatedBill() throws Exception {
        // Given
        Long orderId = 100L;
        BillDto billDto = createMockBillDto(1L);
        when(billingService.generateBill(orderId)).thenReturn(billDto);

        // When & Then
        mockMvc.perform(post("/api/bills/generate/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", is(orderId.intValue())))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalAmount", is(100.00)))
                .andExpect(jsonPath("$.finalAmount", is(115.00)));

        verify(billingService).generateBill(orderId);
    }

    @Test
    void generateBill_WhenBillAlreadyExists_ReturnsConflict() throws Exception {
        // Given
        Long orderId = 100L;
        when(billingService.generateBill(orderId))
                .thenThrow(new BusinessConflictException(
                        "Bill already exists for this order",
                        "Bill",
                        orderId,
                        "Existing bill ID: 1"
                ));

        // When & Then
        mockMvc.perform(post("/api/bills/generate/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        verify(billingService).generateBill(orderId);
    }

    @Test
    void applyDiscount_WhenSuccessful_ReturnsUpdatedBill() throws Exception {
        // Given
        Long billId = 1L;
        BigDecimal discountAmount = new BigDecimal("20.00");
        BillDto updatedBill = createMockBillDto(billId);
        updatedBill.setDiscountAmount(discountAmount);
        updatedBill.setFinalAmount(new BigDecimal("95.00"));

        when(billingService.applyDiscount(eq(billId), any(BigDecimal.class)))
                .thenReturn(updatedBill);

        // When & Then
        mockMvc.perform(patch("/api/bills/{id}/discount", billId)
                        .param("amount", "20.00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(billId.intValue())))
                .andExpect(jsonPath("$.discountAmount", is(20.00)))
                .andExpect(jsonPath("$.finalAmount", is(95.00)));

        verify(billingService).applyDiscount(eq(billId), any(BigDecimal.class));
    }

    @Test
    void applyDiscount_WhenBillNotPending_ReturnsConflict() throws Exception {
        // Given
        Long billId = 1L;
        when(billingService.applyDiscount(eq(billId), any(BigDecimal.class)))
                .thenThrow(new BusinessConflictException(
                        "Cannot apply discount to non-pending bill",
                        "Bill",
                        billId,
                        "Current status: PAID"
                ));

        // When & Then
        mockMvc.perform(patch("/api/bills/{id}/discount", billId)
                        .param("amount", "20.00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        verify(billingService).applyDiscount(eq(billId), any(BigDecimal.class));
    }

    @Test
    void payBill_WhenSuccessful_ReturnsUpdatedBill() throws Exception {
        // Given
        Long billId = 1L;
        PaymentMethod paymentMethod = PaymentMethod.CASH;
        BillDto paidBill = createMockBillDto(billId);
        paidBill.setStatus(BillStatus.PAID);
        paidBill.setPaymentMethod(paymentMethod);

        when(billingService.payBill(billId, paymentMethod)).thenReturn(paidBill);

        // When & Then
        mockMvc.perform(patch("/api/bills/{id}/pay", billId)
                        .param("paymentMethod", "CASH")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(billId.intValue())))
                .andExpect(jsonPath("$.status", is("PAID")))
                .andExpect(jsonPath("$.paymentMethod", is("CASH")));

        verify(billingService).payBill(billId, paymentMethod);
    }

    @Test
    void payBill_WhenBillNotPending_ReturnsConflict() throws Exception {
        // Given
        Long billId = 1L;
        when(billingService.payBill(eq(billId), any(PaymentMethod.class)))
                .thenThrow(new BusinessConflictException(
                        "Cannot pay bill: must be in PENDING status",
                        "Bill",
                        billId,
                        "Current status: PAID"
                ));

        // When & Then
        mockMvc.perform(patch("/api/bills/{id}/pay", billId)
                        .param("paymentMethod", "CASH")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        verify(billingService).payBill(eq(billId), any(PaymentMethod.class));
    }

    @Test
    void payBill_WithDifferentPaymentMethods_ReturnsUpdatedBill() throws Exception {
        // Given
        Long billId = 1L;
        PaymentMethod[] methods = {PaymentMethod.CREDIT_CARD, PaymentMethod.CASH, PaymentMethod.MOBILE_PAYMENT};

        for (PaymentMethod method : methods) {
            BillDto paidBill = createMockBillDto(billId);
            paidBill.setStatus(BillStatus.PAID);
            paidBill.setPaymentMethod(method);
            when(billingService.payBill(billId, method)).thenReturn(paidBill);

            // When & Then
            mockMvc.perform(patch("/api/bills/{id}/pay", billId)
                            .param("paymentMethod", method.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("PAID")))
                    .andExpect(jsonPath("$.paymentMethod", is(method.name())));
        }

        verify(billingService, times(3)).payBill(eq(billId), any(PaymentMethod.class));
    }

    @Test
    void cancelBill_WhenSuccessful_ReturnsUpdatedBill() throws Exception {
        // Given
        Long billId = 1L;
        BillDto cancelledBill = createMockBillDto(billId);
        cancelledBill.setStatus(BillStatus.CANCELLED);

        when(billingService.cancelBill(billId)).thenReturn(cancelledBill);

        // When & Then
        mockMvc.perform(patch("/api/bills/{id}/cancel", billId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(billId.intValue())))
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        verify(billingService).cancelBill(billId);
    }

    @Test
    void cancelBill_WhenBillAlreadyPaid_ReturnsConflict() throws Exception {
        // Given
        Long billId = 1L;
        when(billingService.cancelBill(billId))
                .thenThrow(new BusinessConflictException(
                        "Cannot cancel paid bill",
                        "Bill",
                        billId,
                        "Bill has already been paid"
                ));

        // When & Then
        mockMvc.perform(patch("/api/bills/{id}/cancel", billId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        verify(billingService).cancelBill(billId);
    }

    @Test
    void deleteBill_WhenExists_ReturnsNoContent() throws Exception {
        // Given
        Long billId = 1L;
        doNothing().when(billingService).deleteBill(billId);

        // When & Then
        mockMvc.perform(delete("/api/bills/{id}", billId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(billingService).deleteBill(billId);
    }

    @Test
    void deleteBill_WhenNotExists_ReturnsNotFound() throws Exception {
        // Given
        Long billId = 999L;
        doThrow(new ResourceNotFoundException("Bill not found"))
                .when(billingService).deleteBill(billId);

        // When & Then
        mockMvc.perform(delete("/api/bills/{id}", billId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(billingService).deleteBill(billId);
    }
}
