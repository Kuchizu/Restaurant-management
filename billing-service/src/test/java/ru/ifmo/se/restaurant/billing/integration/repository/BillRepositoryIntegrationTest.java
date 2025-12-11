package ru.ifmo.se.restaurant.billing.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ifmo.se.restaurant.billing.entity.Bill;
import ru.ifmo.se.restaurant.billing.entity.BillStatus;
import ru.ifmo.se.restaurant.billing.entity.PaymentMethod;
import ru.ifmo.se.restaurant.billing.repository.BillRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BillRepositoryIntegrationTest {

    @Autowired
    private BillRepository billRepository;

    @BeforeEach
    void setUp() {
        billRepository.deleteAll();
    }

    @Test
    void findByOrderId_WhenExists_ReturnsBill() {
        // Given
        Long orderId = 100L;
        Bill bill = createBill(orderId, BillStatus.PENDING);
        billRepository.save(bill);

        // When
        Optional<Bill> result = billRepository.findByOrderId(orderId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(orderId);
        assertThat(result.get().getStatus()).isEqualTo(BillStatus.PENDING);
    }

    @Test
    void findByOrderId_WhenNotExists_ReturnsEmpty() {
        // Given
        Long nonExistentOrderId = 999L;

        // When
        Optional<Bill> result = billRepository.findByOrderId(nonExistentOrderId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByOrderId_WhenMultipleBillsExist_ReturnsCorrectOne() {
        // Given
        Bill bill1 = createBill(100L, BillStatus.PENDING);
        Bill bill2 = createBill(200L, BillStatus.PAID);
        Bill bill3 = createBill(300L, BillStatus.CANCELLED);
        billRepository.saveAll(List.of(bill1, bill2, bill3));

        // When
        Optional<Bill> result = billRepository.findByOrderId(200L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(200L);
        assertThat(result.get().getStatus()).isEqualTo(BillStatus.PAID);
    }

    @Test
    void findByStatus_ReturnsAllBillsWithStatus() {
        // Given
        Bill pendingBill1 = createBill(100L, BillStatus.PENDING);
        Bill pendingBill2 = createBill(200L, BillStatus.PENDING);
        Bill paidBill = createBill(300L, BillStatus.PAID);
        billRepository.saveAll(List.of(pendingBill1, pendingBill2, paidBill));

        // When
        List<Bill> result = billRepository.findByStatus(BillStatus.PENDING);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(bill -> bill.getStatus() == BillStatus.PENDING);
        assertThat(result).extracting(Bill::getOrderId)
                .containsExactlyInAnyOrder(100L, 200L);
    }

    @Test
    void findByStatus_WhenNoMatchingStatus_ReturnsEmptyList() {
        // Given
        Bill pendingBill = createBill(100L, BillStatus.PENDING);
        Bill paidBill = createBill(200L, BillStatus.PAID);
        billRepository.saveAll(List.of(pendingBill, paidBill));

        // When
        List<Bill> result = billRepository.findByStatus(BillStatus.CANCELLED);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByStatus_ReturnsAllCancelledBills() {
        // Given
        Bill cancelledBill1 = createBill(100L, BillStatus.CANCELLED);
        Bill cancelledBill2 = createBill(200L, BillStatus.CANCELLED);
        Bill pendingBill = createBill(300L, BillStatus.PENDING);
        billRepository.saveAll(List.of(cancelledBill1, cancelledBill2, pendingBill));

        // When
        List<Bill> result = billRepository.findByStatus(BillStatus.CANCELLED);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(bill -> bill.getStatus() == BillStatus.CANCELLED);
    }

    @Test
    void findByStatus_ReturnsAllPaidBills() {
        // Given
        Bill paidBill1 = createBillWithPayment(100L);
        Bill paidBill2 = createBillWithPayment(200L);
        Bill pendingBill = createBill(300L, BillStatus.PENDING);
        billRepository.saveAll(List.of(paidBill1, paidBill2, pendingBill));

        // When
        List<Bill> result = billRepository.findByStatus(BillStatus.PAID);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(bill -> bill.getStatus() == BillStatus.PAID);
        assertThat(result).allMatch(bill -> bill.getPaymentMethod() != null);
        assertThat(result).allMatch(bill -> bill.getPaidAt() != null);
    }

    @Test
    void save_PersistsBillCorrectly() {
        // Given
        Bill bill = createBill(100L, BillStatus.PENDING);

        // When
        Bill savedBill = billRepository.save(bill);

        // Then
        assertThat(savedBill.getId()).isNotNull();
        assertThat(savedBill.getOrderId()).isEqualTo(100L);
        assertThat(savedBill.getStatus()).isEqualTo(BillStatus.PENDING);
        assertThat(savedBill.getTotalAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void save_UpdatesExistingBill() {
        // Given
        Bill bill = createBill(100L, BillStatus.PENDING);
        Bill savedBill = billRepository.save(bill);

        // When
        savedBill.setStatus(BillStatus.PAID);
        savedBill.setPaymentMethod(PaymentMethod.CASH);
        savedBill.setPaidAt(LocalDateTime.now());
        Bill updatedBill = billRepository.save(savedBill);

        // Then
        assertThat(updatedBill.getId()).isEqualTo(savedBill.getId());
        assertThat(updatedBill.getStatus()).isEqualTo(BillStatus.PAID);
        assertThat(updatedBill.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(updatedBill.getPaidAt()).isNotNull();
    }

    @Test
    void deleteById_RemovesBill() {
        // Given
        Bill bill = createBill(100L, BillStatus.PENDING);
        Bill savedBill = billRepository.save(bill);
        Long billId = savedBill.getId();

        // When
        billRepository.deleteById(billId);

        // Then
        Optional<Bill> result = billRepository.findById(billId);
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ReturnsAllBills() {
        // Given
        Bill bill1 = createBill(100L, BillStatus.PENDING);
        Bill bill2 = createBill(200L, BillStatus.PAID);
        Bill bill3 = createBill(300L, BillStatus.CANCELLED);
        billRepository.saveAll(List.of(bill1, bill2, bill3));

        // When
        List<Bill> result = billRepository.findAll();

        // Then
        assertThat(result).hasSize(3);
    }

    @Test
    void existsById_WhenExists_ReturnsTrue() {
        // Given
        Bill bill = createBill(100L, BillStatus.PENDING);
        Bill savedBill = billRepository.save(bill);

        // When
        boolean exists = billRepository.existsById(savedBill.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WhenNotExists_ReturnsFalse() {
        // Given
        Long nonExistentId = 999L;

        // When
        boolean exists = billRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByOrderId_WithComplexBillData_PreservesAllFields() {
        // Given
        Long orderId = 100L;
        Bill bill = new Bill();
        bill.setOrderId(orderId);
        bill.setTotalAmount(new BigDecimal("150.50"));
        bill.setTaxAmount(new BigDecimal("15.05"));
        bill.setServiceCharge(new BigDecimal("7.53"));
        bill.setDiscountAmount(new BigDecimal("10.00"));
        bill.setFinalAmount(new BigDecimal("163.08"));
        bill.setStatus(BillStatus.PENDING);
        bill.setCreatedAt(LocalDateTime.now());
        bill.setNotes("Special discount applied");
        billRepository.save(bill);

        // When
        Optional<Bill> result = billRepository.findByOrderId(orderId);

        // Then
        assertThat(result).isPresent();
        Bill foundBill = result.get();
        assertThat(foundBill.getTotalAmount()).isEqualByComparingTo("150.50");
        assertThat(foundBill.getTaxAmount()).isEqualByComparingTo("15.05");
        assertThat(foundBill.getServiceCharge()).isEqualByComparingTo("7.53");
        assertThat(foundBill.getDiscountAmount()).isEqualByComparingTo("10.00");
        assertThat(foundBill.getFinalAmount()).isEqualByComparingTo("163.08");
        assertThat(foundBill.getNotes()).isEqualTo("Special discount applied");
    }

    @Test
    void findByStatus_WithPaymentMethods_PreservesPaymentInfo() {
        // Given
        Bill cashBill = createBillWithPayment(100L, PaymentMethod.CASH);
        Bill cardBill = createBillWithPayment(200L, PaymentMethod.CREDIT_CARD);
        Bill walletBill = createBillWithPayment(300L, PaymentMethod.MOBILE_PAYMENT);
        billRepository.saveAll(List.of(cashBill, cardBill, walletBill));

        // When
        List<Bill> result = billRepository.findByStatus(BillStatus.PAID);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Bill::getPaymentMethod)
                .containsExactlyInAnyOrder(PaymentMethod.CASH, PaymentMethod.CREDIT_CARD, PaymentMethod.MOBILE_PAYMENT);
    }

    // Helper methods

    private Bill createBill(Long orderId, BillStatus status) {
        Bill bill = new Bill();
        bill.setOrderId(orderId);
        bill.setTotalAmount(new BigDecimal("100.00"));
        bill.setTaxAmount(new BigDecimal("10.00"));
        bill.setServiceCharge(new BigDecimal("5.00"));
        bill.setDiscountAmount(BigDecimal.ZERO);
        bill.setFinalAmount(new BigDecimal("115.00"));
        bill.setStatus(status);
        bill.setCreatedAt(LocalDateTime.now());
        return bill;
    }

    private Bill createBillWithPayment(Long orderId) {
        return createBillWithPayment(orderId, PaymentMethod.CASH);
    }

    private Bill createBillWithPayment(Long orderId, PaymentMethod paymentMethod) {
        Bill bill = createBill(orderId, BillStatus.PAID);
        bill.setPaymentMethod(paymentMethod);
        bill.setPaidAt(LocalDateTime.now());
        return bill;
    }
}
