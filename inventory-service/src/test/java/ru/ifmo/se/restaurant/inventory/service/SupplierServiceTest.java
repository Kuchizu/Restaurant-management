package ru.ifmo.se.restaurant.inventory.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.inventory.dataaccess.*;
import ru.ifmo.se.restaurant.inventory.dto.SupplierDto;
import ru.ifmo.se.restaurant.inventory.dto.SupplyOrderDto;
import ru.ifmo.se.restaurant.inventory.dto.SupplyOrderItemDto;
import ru.ifmo.se.restaurant.inventory.entity.*;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.exception.ValidationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.inventory.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierDataAccess supplierDataAccess;

    @Mock
    private SupplyOrderDataAccess supplyOrderDataAccess;

    @Mock
    private SupplyOrderIngredientDataAccess supplyOrderIngredientDataAccess;

    @Mock
    private IngredientDataAccess ingredientDataAccess;

    @Mock
    private InventoryDataAccess inventoryDataAccess;

    @InjectMocks
    private SupplierService supplierService;

    // ========== SUPPLIER TESTS ==========

    @Test
    void getAllSuppliers_ReturnsAllSupplierDtos() {
        // Given
        List<Supplier> suppliers = Arrays.asList(
                createMockSupplier(1L),
                createMockSupplier(2L)
        );
        when(supplierDataAccess.findAll()).thenReturn(suppliers);

        // When
        List<SupplierDto> result = supplierService.getAllSuppliers();

        // Then
        assertThat(result).hasSize(2);
        verify(supplierDataAccess).findAll();
    }

    @Test
    void getSupplierById_WhenExists_ReturnsSupplierDto() {
        // Given
        Supplier supplier = createMockSupplier(1L);
        when(supplierDataAccess.getById(1L)).thenReturn(supplier);

        // When
        SupplierDto result = supplierService.getSupplierById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).contains("Test Supplier");
        verify(supplierDataAccess).getById(1L);
    }

    @Test
    void createSupplier_SavesSupplierAndReturnsDto() {
        // Given
        SupplierDto inputDto = createMockSupplierDto(null);
        Supplier savedSupplier = createMockSupplier(1L);

        when(supplierDataAccess.save(any(Supplier.class))).thenReturn(savedSupplier);

        // When
        SupplierDto result = supplierService.createSupplier(inputDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(supplierDataAccess).save(any(Supplier.class));
    }

    @Test
    void updateSupplier_WhenExists_UpdatesAllFields() {
        // Given
        Supplier supplier = createMockSupplier(1L);
        SupplierDto updateDto = new SupplierDto();
        updateDto.setName("Updated Supplier");
        updateDto.setContactPerson("Jane Doe");
        updateDto.setPhone("+9876543210");
        updateDto.setEmail("updated@test.com");
        updateDto.setAddress("456 New Street");

        when(supplierDataAccess.getById(1L)).thenReturn(supplier);
        when(supplierDataAccess.save(any(Supplier.class))).thenReturn(supplier);

        // When
        SupplierDto result = supplierService.updateSupplier(1L, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(supplierDataAccess).save(argThat(s ->
                s.getName().equals("Updated Supplier") &&
                        s.getContactPerson().equals("Jane Doe") &&
                        s.getPhone().equals("+9876543210") &&
                        s.getEmail().equals("updated@test.com") &&
                        s.getAddress().equals("456 New Street")
        ));
    }

    @Test
    void deleteSupplier_WhenExists_DeletesSupplier() {
        // Given
        when(supplierDataAccess.existsById(1L)).thenReturn(true);
        doNothing().when(supplierDataAccess).deleteById(1L);

        // When
        supplierService.deleteSupplier(1L);

        // Then
        verify(supplierDataAccess).existsById(1L);
        verify(supplierDataAccess).deleteById(1L);
    }

    @Test
    void deleteSupplier_WhenNotExists_ThrowsResourceNotFoundException() {
        // Given
        when(supplierDataAccess.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> supplierService.deleteSupplier(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found");

        verify(supplierDataAccess, never()).deleteById(any());
    }

    // ========== SUPPLY ORDER TESTS ==========

    @Test
    void getAllSupplyOrders_ReturnsAllSupplyOrderDtos() {
        // Given
        Supplier supplier = createMockSupplier(1L);
        List<SupplyOrder> orders = Arrays.asList(
                createMockSupplyOrder(1L, supplier),
                createMockSupplyOrder(2L, supplier)
        );
        when(supplyOrderDataAccess.findAll()).thenReturn(orders);
        when(supplyOrderIngredientDataAccess.findBySupplyOrderId(anyLong()))
                .thenReturn(new ArrayList<>());

        // When
        List<SupplyOrderDto> result = supplierService.getAllSupplyOrders();

        // Then
        assertThat(result).hasSize(2);
        verify(supplyOrderDataAccess).findAll();
    }

    @Test
    void getSupplyOrderById_WhenExists_ReturnsSupplyOrderDto() {
        // Given
        Supplier supplier = createMockSupplier(1L);
        SupplyOrder order = createMockSupplyOrder(1L, supplier);

        when(supplyOrderDataAccess.getById(1L)).thenReturn(order);
        when(supplyOrderIngredientDataAccess.findBySupplyOrderId(1L))
                .thenReturn(new ArrayList<>());

        // When
        SupplyOrderDto result = supplierService.getSupplyOrderById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSupplierId()).isEqualTo(1L);
        verify(supplyOrderDataAccess).getById(1L);
    }

    @Test
    void getSupplyOrdersByStatus_ReturnsMatchingOrders() {
        // Given
        Supplier supplier = createMockSupplier(1L);
        List<SupplyOrder> pendingOrders = Arrays.asList(
                createSupplyOrderWithStatus(1L, supplier, SupplyOrderStatus.PENDING),
                createSupplyOrderWithStatus(2L, supplier, SupplyOrderStatus.PENDING)
        );

        when(supplyOrderDataAccess.findByStatus(SupplyOrderStatus.PENDING))
                .thenReturn(pendingOrders);
        when(supplyOrderIngredientDataAccess.findBySupplyOrderId(anyLong()))
                .thenReturn(new ArrayList<>());

        // When
        List<SupplyOrderDto> result = supplierService.getSupplyOrdersByStatus(SupplyOrderStatus.PENDING);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(dto -> dto.getStatus() == SupplyOrderStatus.PENDING);
        verify(supplyOrderDataAccess).findByStatus(SupplyOrderStatus.PENDING);
    }

    @Test
    void createSupplyOrder_WithValidSupplier_CreatesOrder() {
        // Given
        SupplyOrderDto inputDto = createMockSupplyOrderDto(null);
        inputDto.setSupplierId(1L);
        Supplier supplier = createMockSupplier(1L);
        SupplyOrder savedOrder = createMockSupplyOrder(1L, supplier);

        when(supplierDataAccess.getById(1L)).thenReturn(supplier);
        when(supplyOrderDataAccess.save(any(SupplyOrder.class))).thenReturn(savedOrder);
        when(supplyOrderIngredientDataAccess.findBySupplyOrderId(1L))
                .thenReturn(new ArrayList<>());

        // When
        SupplyOrderDto result = supplierService.createSupplyOrder(inputDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SupplyOrderStatus.PENDING);
        verify(supplierDataAccess).getById(1L);
        verify(supplyOrderDataAccess, times(2)).save(any(SupplyOrder.class));
    }

    @Test
    void createSupplyOrder_WithItems_CreatesOrderAndItems() {
        // Given
        SupplyOrderDto inputDto = createCompleteSupplyOrderDto(null, 1L);
        Supplier supplier = createMockSupplier(1L);
        SupplyOrder savedOrder = createMockSupplyOrder(1L, supplier);
        Ingredient ingredient = createMockIngredient(1L);

        when(supplierDataAccess.getById(1L)).thenReturn(supplier);
        when(supplyOrderDataAccess.save(any(SupplyOrder.class))).thenReturn(savedOrder);
        when(ingredientDataAccess.getById(anyLong())).thenReturn(ingredient);
        when(supplyOrderIngredientDataAccess.save(any(SupplyOrderIngredient.class)))
                .thenAnswer(inv -> {
                    SupplyOrderIngredient item = inv.getArgument(0);
                    if (item.getId() == null) {
                        item.setId(1L);
                    }
                    return item;
                });
        List<SupplyOrderIngredient> items = new ArrayList<>();
        when(supplyOrderIngredientDataAccess.findBySupplyOrderId(1L))
                .thenReturn(items);

        // When
        SupplyOrderDto result = supplierService.createSupplyOrder(inputDto);

        // Then
        assertThat(result).isNotNull();
        verify(supplyOrderIngredientDataAccess, times(2)).save(any(SupplyOrderIngredient.class));
        verify(supplyOrderDataAccess, atLeast(2)).save(any(SupplyOrder.class));
    }

    @Test
    void createSupplyOrder_WhenSupplierNotFound_ThrowsResourceNotFoundException() {
        // Given
        SupplyOrderDto inputDto = createMockSupplyOrderDto(null);
        inputDto.setSupplierId(999L);

        when(supplierDataAccess.getById(999L))
                .thenThrow(new ResourceNotFoundException("Supplier not found"));

        // When & Then
        assertThatThrownBy(() -> supplierService.createSupplyOrder(inputDto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(supplyOrderDataAccess, never()).save(any());
    }

    @Test
    void updateSupplyOrderStatus_ToDelivered_UpdatesInventory() {
        // Given
        Supplier supplier = createMockSupplier(1L);
        SupplyOrder order = createSupplyOrderWithStatus(1L, supplier, SupplyOrderStatus.PENDING);
        Ingredient ingredient = createMockIngredient(1L);
        Inventory inventory = createMockInventory(1L, ingredient);

        SupplyOrderIngredient orderItem = createMockSupplyOrderIngredient(
                1L, order, ingredient,
                new BigDecimal("50.00"),
                new BigDecimal("10.00")
        );

        when(supplyOrderDataAccess.getById(1L)).thenReturn(order);
        when(supplyOrderIngredientDataAccess.findBySupplyOrderId(1L))
                .thenReturn(Arrays.asList(orderItem));
        when(inventoryDataAccess.findByIngredientId(1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryDataAccess.save(any(Inventory.class))).thenReturn(inventory);
        when(supplyOrderDataAccess.save(any(SupplyOrder.class))).thenReturn(order);

        // When
        SupplyOrderDto result = supplierService.updateSupplyOrderStatus(1L, SupplyOrderStatus.DELIVERED);

        // Then
        assertThat(result.getStatus()).isEqualTo(SupplyOrderStatus.DELIVERED);
        assertThat(result.getDeliveryDate()).isNotNull();
        verify(inventoryDataAccess).save(argThat(inv ->
                inv.getQuantity().compareTo(new BigDecimal("150.00")) == 0
        ));
        verify(supplyOrderDataAccess).save(argThat(o ->
                o.getStatus() == SupplyOrderStatus.DELIVERED &&
                        o.getDeliveryDate() != null
        ));
    }

    @Test
    void updateSupplyOrderStatus_ToCancelled_DoesNotUpdateInventory() {
        // Given
        Supplier supplier = createMockSupplier(1L);
        SupplyOrder order = createSupplyOrderWithStatus(1L, supplier, SupplyOrderStatus.PENDING);

        when(supplyOrderDataAccess.getById(1L)).thenReturn(order);
        when(supplyOrderDataAccess.save(any(SupplyOrder.class))).thenReturn(order);
        when(supplyOrderIngredientDataAccess.findBySupplyOrderId(1L))
                .thenReturn(new ArrayList<>());

        // When
        SupplyOrderDto result = supplierService.updateSupplyOrderStatus(1L, SupplyOrderStatus.CANCELLED);

        // Then
        assertThat(result.getStatus()).isEqualTo(SupplyOrderStatus.CANCELLED);
        verify(inventoryDataAccess, never()).save(any());
    }

    @Test
    void deleteSupplyOrder_WhenExists_DeletesOrder() {
        // Given
        when(supplyOrderDataAccess.existsById(1L)).thenReturn(true);
        doNothing().when(supplyOrderDataAccess).deleteById(1L);

        // When
        supplierService.deleteSupplyOrder(1L);

        // Then
        verify(supplyOrderDataAccess).existsById(1L);
        verify(supplyOrderDataAccess).deleteById(1L);
    }

    @Test
    void toSupplyOrderDto_WhenOrderHasNullSupplier_ThrowsValidationException() {
        // Given
        SupplyOrder order = new SupplyOrder();
        order.setId(1L);
        order.setSupplier(null); // Null supplier

        when(supplyOrderDataAccess.getById(1L)).thenReturn(order);

        // When & Then
        assertThatThrownBy(() -> supplierService.getSupplyOrderById(1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Supply order has no supplier assigned");
    }
}
