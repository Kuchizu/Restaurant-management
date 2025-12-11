package ru.ifmo.se.restaurant.billing.dataaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.billing.entity.Bill;
import ru.ifmo.se.restaurant.billing.entity.BillStatus;
import ru.ifmo.se.restaurant.billing.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.billing.repository.BillRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.billing.TestDataFactory.createMockBill;

@ExtendWith(MockitoExtension.class)
class BillDataAccessTest {

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillDataAccess billDataAccess;

    @Test
    void findById_WhenExists_ReturnsOptionalWithBill() {
        // Given
        Long billId = 1L;
        Bill bill = createMockBill(billId);
        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));

        // When
        Optional<Bill> result = billDataAccess.findById(billId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(billId);
        verify(billRepository).findById(billId);
    }

    @Test
    void findById_WhenNotExists_ReturnsEmptyOptional() {
        // Given
        Long billId = 999L;
        when(billRepository.findById(billId)).thenReturn(Optional.empty());

        // When
        Optional<Bill> result = billDataAccess.findById(billId);

        // Then
        assertThat(result).isEmpty();
        verify(billRepository).findById(billId);
    }

    @Test
    void getById_WhenExists_ReturnsBill() {
        // Given
        Long billId = 1L;
        Bill bill = createMockBill(billId);
        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));

        // When
        Bill result = billDataAccess.getById(billId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(billId);
        verify(billRepository).findById(billId);
    }

    @Test
    void getById_WhenNotExists_ThrowsException() {
        // Given
        Long billId = 999L;
        when(billRepository.findById(billId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> billDataAccess.getById(billId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Bill not found");

        verify(billRepository).findById(billId);
    }

    @Test
    void save_SavesBillAndReturns() {
        // Given
        Bill bill = createMockBill(null);
        Bill savedBill = createMockBill(1L);
        when(billRepository.save(bill)).thenReturn(savedBill);

        // When
        Bill result = billDataAccess.save(bill);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(billRepository).save(bill);
    }

    @Test
    void findAll_ReturnsAllBills() {
        // Given
        List<Bill> bills = Arrays.asList(
                createMockBill(1L),
                createMockBill(2L)
        );
        when(billRepository.findAll()).thenReturn(bills);

        // When
        List<Bill> result = billDataAccess.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(billRepository).findAll();
    }

    @Test
    void findByOrderId_WhenExists_ReturnsOptionalWithBill() {
        // Given
        Long orderId = 100L;
        Bill bill = createMockBill(1L);
        when(billRepository.findByOrderId(orderId)).thenReturn(Optional.of(bill));

        // When
        Optional<Bill> result = billDataAccess.findByOrderId(orderId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(orderId);
        verify(billRepository).findByOrderId(orderId);
    }

    @Test
    void findByStatus_ReturnsMatchingBills() {
        // Given
        BillStatus status = BillStatus.PENDING;
        List<Bill> bills = Arrays.asList(
                createMockBill(1L),
                createMockBill(2L)
        );
        when(billRepository.findByStatus(status)).thenReturn(bills);

        // When
        List<Bill> result = billDataAccess.findByStatus(status);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(b -> b.getStatus() == BillStatus.PENDING);
        verify(billRepository).findByStatus(status);
    }

    @Test
    void existsById_WhenExists_ReturnsTrue() {
        // Given
        Long billId = 1L;
        when(billRepository.existsById(billId)).thenReturn(true);

        // When
        boolean result = billDataAccess.existsById(billId);

        // Then
        assertThat(result).isTrue();
        verify(billRepository).existsById(billId);
    }

    @Test
    void existsById_WhenNotExists_ReturnsFalse() {
        // Given
        Long billId = 999L;
        when(billRepository.existsById(billId)).thenReturn(false);

        // When
        boolean result = billDataAccess.existsById(billId);

        // Then
        assertThat(result).isFalse();
        verify(billRepository).existsById(billId);
    }

    @Test
    void deleteById_DeletesBill() {
        // Given
        Long billId = 1L;
        doNothing().when(billRepository).deleteById(billId);

        // When
        billDataAccess.deleteById(billId);

        // Then
        verify(billRepository).deleteById(billId);
    }
}
