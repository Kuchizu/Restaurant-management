package ru.ifmo.se.restaurant.billing.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.ifmo.se.restaurant.billing.application.dto.BillDto;
import ru.ifmo.se.restaurant.billing.application.port.in.DeleteBillUseCase;
import ru.ifmo.se.restaurant.billing.application.port.in.GenerateBillUseCase;
import ru.ifmo.se.restaurant.billing.application.port.in.GetBillUseCase;
import ru.ifmo.se.restaurant.billing.application.port.in.UpdateBillUseCase;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;
import ru.ifmo.se.restaurant.billing.domain.valueobject.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BillingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GetBillUseCase getBillUseCase;

    @Mock
    private GenerateBillUseCase generateBillUseCase;

    @Mock
    private UpdateBillUseCase updateBillUseCase;

    @Mock
    private DeleteBillUseCase deleteBillUseCase;

    @InjectMocks
    private BillingController billingController;

    private BillDto testBill;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(billingController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(converter)
                .build();

        testBill = new BillDto();
        testBill.setId(1L);
        testBill.setOrderId(100L);
        testBill.setTotalAmount(new BigDecimal("100.00"));
        testBill.setTaxAmount(new BigDecimal("10.00"));
        testBill.setServiceCharge(new BigDecimal("5.00"));
        testBill.setDiscountAmount(BigDecimal.ZERO);
        testBill.setFinalAmount(new BigDecimal("115.00"));
        testBill.setStatus(BillStatus.PENDING);
        testBill.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllBills_ShouldReturnList() throws Exception {
        when(getBillUseCase.getAllBills()).thenReturn(Arrays.asList(testBill));

        mockMvc.perform(get("/api/bills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getBillById_ShouldReturnBill() throws Exception {
        when(getBillUseCase.getBillById(1L)).thenReturn(testBill);

        mockMvc.perform(get("/api/bills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getBillByOrderId_ShouldReturnBill() throws Exception {
        when(getBillUseCase.getBillByOrderId(100L)).thenReturn(testBill);

        mockMvc.perform(get("/api/bills/order/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(100));
    }

    @Test
    void getBillsByStatus_ShouldReturnList() throws Exception {
        when(getBillUseCase.getBillsByStatus(BillStatus.PENDING))
                .thenReturn(Arrays.asList(testBill));

        mockMvc.perform(get("/api/bills/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void generateBill_ShouldReturn201() throws Exception {
        when(generateBillUseCase.generateBill(100L)).thenReturn(testBill);

        mockMvc.perform(post("/api/bills/generate/100"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(100));
    }

    @Test
    void applyDiscount_ShouldReturnUpdatedBill() throws Exception {
        testBill.setDiscountAmount(new BigDecimal("10.00"));
        when(updateBillUseCase.applyDiscount(eq(1L), any(BigDecimal.class)))
                .thenReturn(testBill);

        mockMvc.perform(patch("/api/bills/1/discount")
                        .param("amount", "10.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountAmount").value(10.00));
    }

    @Test
    void payBill_ShouldReturnPaidBill() throws Exception {
        testBill.setStatus(BillStatus.PAID);
        testBill.setPaymentMethod(PaymentMethod.CASH);
        when(updateBillUseCase.payBill(1L, PaymentMethod.CASH)).thenReturn(testBill);

        mockMvc.perform(patch("/api/bills/1/pay")
                        .param("paymentMethod", "CASH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void cancelBill_ShouldReturnCancelledBill() throws Exception {
        testBill.setStatus(BillStatus.CANCELLED);
        when(updateBillUseCase.cancelBill(1L)).thenReturn(testBill);

        mockMvc.perform(patch("/api/bills/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void deleteBill_ShouldReturn204() throws Exception {
        doNothing().when(deleteBillUseCase).deleteBill(1L);

        mockMvc.perform(delete("/api/bills/1"))
                .andExpect(status().isNoContent());

        verify(deleteBillUseCase).deleteBill(1L);
    }

    @Test
    void payBill_WithCard_ShouldReturnPaidBill() throws Exception {
        testBill.setStatus(BillStatus.PAID);
        testBill.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        when(updateBillUseCase.payBill(1L, PaymentMethod.CREDIT_CARD)).thenReturn(testBill);

        mockMvc.perform(patch("/api/bills/1/pay")
                        .param("paymentMethod", "CREDIT_CARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"));
    }
}
