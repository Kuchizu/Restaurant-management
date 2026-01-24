package ru.ifmo.se.restaurant.inventory.domain.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.inventory.domain.valueobject.SupplyOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SupplyOrderTest {

    private Supplier createTestSupplier() {
        return Supplier.builder()
                .id(1L)
                .name("Test Supplier")
                .contactPerson("John Doe")
                .phone("123-456-7890")
                .email("supplier@test.com")
                .address("123 Test Street")
                .build();
    }

    @Test
    void builder_ShouldCreateSupplyOrder() {
        Supplier supplier = createTestSupplier();
        LocalDateTime now = LocalDateTime.now();

        SupplyOrder order = SupplyOrder.builder()
                .id(1L)
                .supplier(supplier)
                .orderDate(now)
                .status(SupplyOrderStatus.PENDING)
                .totalCost(new BigDecimal("100.00"))
                .notes("Test notes")
                .items(Collections.emptyList())
                .build();

        assertEquals(1L, order.getId());
        assertEquals(supplier, order.getSupplier());
        assertEquals(now, order.getOrderDate());
        assertEquals(SupplyOrderStatus.PENDING, order.getStatus());
        assertEquals(new BigDecimal("100.00"), order.getTotalCost());
        assertEquals("Test notes", order.getNotes());
    }

    @Test
    void updateStatus_ShouldUpdateToNewStatus() {
        SupplyOrder order = SupplyOrder.builder()
                .id(1L)
                .supplier(createTestSupplier())
                .orderDate(LocalDateTime.now())
                .status(SupplyOrderStatus.PENDING)
                .build();

        SupplyOrder updated = order.updateStatus(SupplyOrderStatus.CONFIRMED);

        assertEquals(SupplyOrderStatus.CONFIRMED, updated.getStatus());
        assertNull(updated.getDeliveryDate());
    }

    @Test
    void updateStatus_ToDelivered_ShouldSetDeliveryDate() {
        SupplyOrder order = SupplyOrder.builder()
                .id(1L)
                .supplier(createTestSupplier())
                .orderDate(LocalDateTime.now())
                .status(SupplyOrderStatus.SHIPPED)
                .build();

        SupplyOrder updated = order.updateStatus(SupplyOrderStatus.DELIVERED);

        assertEquals(SupplyOrderStatus.DELIVERED, updated.getStatus());
        assertNotNull(updated.getDeliveryDate());
    }

    @Test
    void updateStatus_ToDelivered_ShouldNotOverwriteExistingDeliveryDate() {
        LocalDateTime existingDeliveryDate = LocalDateTime.of(2024, 1, 1, 10, 0);

        SupplyOrder order = SupplyOrder.builder()
                .id(1L)
                .supplier(createTestSupplier())
                .orderDate(LocalDateTime.now())
                .deliveryDate(existingDeliveryDate)
                .status(SupplyOrderStatus.SHIPPED)
                .build();

        SupplyOrder updated = order.updateStatus(SupplyOrderStatus.DELIVERED);

        assertEquals(existingDeliveryDate, updated.getDeliveryDate());
    }

    @Test
    void isDelivered_ShouldReturnTrue_WhenStatusIsDelivered() {
        SupplyOrder order = SupplyOrder.builder()
                .id(1L)
                .supplier(createTestSupplier())
                .status(SupplyOrderStatus.DELIVERED)
                .build();

        assertTrue(order.isDelivered());
    }

    @Test
    void isDelivered_ShouldReturnFalse_WhenStatusIsNotDelivered() {
        SupplyOrder order = SupplyOrder.builder()
                .id(1L)
                .supplier(createTestSupplier())
                .status(SupplyOrderStatus.PENDING)
                .build();

        assertFalse(order.isDelivered());
    }

    @Test
    void canBeDelivered_ShouldReturnTrue_WhenStatusIsShipped() {
        SupplyOrder order = SupplyOrder.builder()
                .id(1L)
                .supplier(createTestSupplier())
                .status(SupplyOrderStatus.SHIPPED)
                .build();

        assertTrue(order.canBeDelivered());
    }

    @Test
    void canBeDelivered_ShouldReturnTrue_WhenStatusIsConfirmed() {
        SupplyOrder order = SupplyOrder.builder()
                .id(1L)
                .supplier(createTestSupplier())
                .status(SupplyOrderStatus.CONFIRMED)
                .build();

        assertTrue(order.canBeDelivered());
    }

    @Test
    void canBeDelivered_ShouldReturnFalse_WhenStatusIsPending() {
        SupplyOrder order = SupplyOrder.builder()
                .id(1L)
                .supplier(createTestSupplier())
                .status(SupplyOrderStatus.PENDING)
                .build();

        assertFalse(order.canBeDelivered());
    }

    @Test
    void canBeDelivered_ShouldReturnFalse_WhenStatusIsCancelled() {
        SupplyOrder order = SupplyOrder.builder()
                .id(1L)
                .supplier(createTestSupplier())
                .status(SupplyOrderStatus.CANCELLED)
                .build();

        assertFalse(order.canBeDelivered());
    }
}
