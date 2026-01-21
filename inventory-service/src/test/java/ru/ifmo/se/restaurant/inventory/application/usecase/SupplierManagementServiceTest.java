package ru.ifmo.se.restaurant.inventory.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import ru.ifmo.se.restaurant.inventory.application.dto.SupplierDto;
import ru.ifmo.se.restaurant.inventory.application.dto.SupplyOrderDto;
import ru.ifmo.se.restaurant.inventory.application.dto.SupplyOrderItemDto;
import ru.ifmo.se.restaurant.inventory.application.port.out.IngredientRepository;
import ru.ifmo.se.restaurant.inventory.application.port.out.InventoryRepository;
import ru.ifmo.se.restaurant.inventory.application.port.out.SupplierRepository;
import ru.ifmo.se.restaurant.inventory.application.port.out.SupplyOrderItemRepository;
import ru.ifmo.se.restaurant.inventory.application.port.out.SupplyOrderRepository;
import ru.ifmo.se.restaurant.inventory.domain.entity.Ingredient;
import ru.ifmo.se.restaurant.inventory.domain.entity.Inventory;
import ru.ifmo.se.restaurant.inventory.domain.entity.Supplier;
import ru.ifmo.se.restaurant.inventory.domain.entity.SupplyOrder;
import ru.ifmo.se.restaurant.inventory.domain.entity.SupplyOrderItem;
import ru.ifmo.se.restaurant.inventory.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.domain.valueobject.SupplyOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierManagementServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplyOrderRepository supplyOrderRepository;

    @Mock
    private SupplyOrderItemRepository supplyOrderItemRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private SupplierManagementService service;

    private Supplier testSupplier;
    private SupplyOrder testSupplyOrder;
    private Ingredient testIngredient;
    private SupplyOrderItem testSupplyOrderItem;

    @BeforeEach
    void setUp() {
        testSupplier = Supplier.builder()
                .id(1L)
                .name("Test Supplier")
                .contactPerson("John Doe")
                .phone("123-456-7890")
                .email("supplier@test.com")
                .address("123 Test Street")
                .build();

        testIngredient = Ingredient.builder()
                .id(1L)
                .name("Salt")
                .unit("kg")
                .build();

        testSupplyOrderItem = SupplyOrderItem.builder()
                .id(1L)
                .ingredient(testIngredient)
                .quantity(new BigDecimal("10"))
                .unitPrice(new BigDecimal("5.00"))
                .build();

        testSupplyOrder = SupplyOrder.builder()
                .id(1L)
                .supplier(testSupplier)
                .orderDate(LocalDateTime.now())
                .status(SupplyOrderStatus.PENDING)
                .totalCost(new BigDecimal("50.00"))
                .notes("Test order")
                .items(Collections.singletonList(testSupplyOrderItem))
                .build();
    }

    // Supplier tests

    @Test
    void getAllSuppliers_ShouldReturnList() {
        when(supplierRepository.findAll()).thenReturn(Arrays.asList(testSupplier));

        List<SupplierDto> result = service.getAllSuppliers();

        assertEquals(1, result.size());
        assertEquals("Test Supplier", result.get(0).getName());
    }

    @Test
    void getAllSuppliersPaginated_ShouldReturnPage() {
        Page<Supplier> page = new PageImpl<>(Arrays.asList(testSupplier));
        when(supplierRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<SupplierDto> result = service.getAllSuppliersPaginated(0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllSuppliersSlice_ShouldReturnSlice() {
        Slice<Supplier> slice = new SliceImpl<>(Arrays.asList(testSupplier));
        when(supplierRepository.findAllSlice(any(Pageable.class))).thenReturn(slice);

        Slice<SupplierDto> result = service.getAllSuppliersSlice(0, 10);

        assertTrue(result.hasContent());
    }

    @Test
    void getSupplierById_ShouldReturnSupplier() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        SupplierDto result = service.getSupplierById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Test Supplier", result.getName());
    }

    @Test
    void getSupplierById_ShouldThrow_WhenNotFound() {
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSupplierById(999L));
    }

    @Test
    void createSupplier_ShouldCreateSupplier() {
        SupplierDto dto = new SupplierDto();
        dto.setName("New Supplier");
        dto.setContactPerson("Jane Doe");
        dto.setPhone("987-654-3210");
        dto.setEmail("new@supplier.com");
        dto.setAddress("456 New Street");

        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);

        SupplierDto result = service.createSupplier(dto);

        assertNotNull(result);
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void updateSupplier_ShouldUpdateSupplier() {
        SupplierDto dto = new SupplierDto();
        dto.setName("Updated Supplier");

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplierDto result = service.updateSupplier(1L, dto);

        assertNotNull(result);
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void updateSupplier_ShouldThrow_WhenNotFound() {
        SupplierDto dto = new SupplierDto();
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateSupplier(999L, dto));
    }

    @Test
    void deleteSupplier_ShouldDelete() {
        when(supplierRepository.existsById(1L)).thenReturn(true);
        doNothing().when(supplierRepository).deleteById(1L);

        assertDoesNotThrow(() -> service.deleteSupplier(1L));
        verify(supplierRepository).deleteById(1L);
    }

    @Test
    void deleteSupplier_ShouldThrow_WhenNotFound() {
        when(supplierRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteSupplier(999L));
    }

    // Supply Order tests

    @Test
    void getAllSupplyOrders_ShouldReturnList() {
        when(supplyOrderRepository.findAll()).thenReturn(Arrays.asList(testSupplyOrder));
        when(supplyOrderItemRepository.findBySupplyOrderId(anyLong())).thenReturn(Collections.singletonList(testSupplyOrderItem));

        List<SupplyOrderDto> result = service.getAllSupplyOrders();

        assertEquals(1, result.size());
    }

    @Test
    void getAllSupplyOrdersPaginated_ShouldReturnPage() {
        Page<SupplyOrder> page = new PageImpl<>(Arrays.asList(testSupplyOrder));
        when(supplyOrderRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(supplyOrderItemRepository.findBySupplyOrderId(anyLong())).thenReturn(Collections.singletonList(testSupplyOrderItem));

        Page<SupplyOrderDto> result = service.getAllSupplyOrdersPaginated(0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllSupplyOrdersSlice_ShouldReturnSlice() {
        Slice<SupplyOrder> slice = new SliceImpl<>(Arrays.asList(testSupplyOrder));
        when(supplyOrderRepository.findAllSlice(any(Pageable.class))).thenReturn(slice);
        when(supplyOrderItemRepository.findBySupplyOrderId(anyLong())).thenReturn(Collections.singletonList(testSupplyOrderItem));

        Slice<SupplyOrderDto> result = service.getAllSupplyOrdersSlice(0, 10);

        assertTrue(result.hasContent());
    }

    @Test
    void getSupplyOrderById_ShouldReturnOrder() {
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(testSupplyOrder));
        when(supplyOrderItemRepository.findBySupplyOrderId(1L)).thenReturn(Collections.singletonList(testSupplyOrderItem));

        SupplyOrderDto result = service.getSupplyOrderById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getSupplyOrderById_ShouldThrow_WhenNotFound() {
        when(supplyOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSupplyOrderById(999L));
    }

    @Test
    void getSupplyOrdersByStatus_ShouldReturnFilteredOrders() {
        when(supplyOrderRepository.findByStatus(SupplyOrderStatus.PENDING)).thenReturn(Arrays.asList(testSupplyOrder));
        when(supplyOrderItemRepository.findBySupplyOrderId(anyLong())).thenReturn(Collections.singletonList(testSupplyOrderItem));

        List<SupplyOrderDto> result = service.getSupplyOrdersByStatus(SupplyOrderStatus.PENDING);

        assertEquals(1, result.size());
    }

    @Test
    void createSupplyOrder_ShouldCreateOrder() {
        SupplyOrderItemDto itemDto = new SupplyOrderItemDto();
        itemDto.setIngredientId(1L);
        itemDto.setQuantity(new BigDecimal("10"));
        itemDto.setUnitPrice(new BigDecimal("5.00"));

        SupplyOrderDto dto = new SupplyOrderDto();
        dto.setSupplierId(1L);
        dto.setNotes("New order");
        dto.setItems(Collections.singletonList(itemDto));

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(supplyOrderRepository.save(any(SupplyOrder.class))).thenReturn(testSupplyOrder);
        when(supplyOrderItemRepository.save(any(SupplyOrderItem.class))).thenReturn(testSupplyOrderItem);
        when(supplyOrderItemRepository.findBySupplyOrderId(anyLong())).thenReturn(Collections.singletonList(testSupplyOrderItem));

        SupplyOrderDto result = service.createSupplyOrder(dto);

        assertNotNull(result);
        verify(supplyOrderRepository).save(any(SupplyOrder.class));
    }

    @Test
    void createSupplyOrder_ShouldThrow_WhenSupplierNotFound() {
        SupplyOrderDto dto = new SupplyOrderDto();
        dto.setSupplierId(999L);

        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createSupplyOrder(dto));
    }

    @Test
    void createSupplyOrder_ShouldThrow_WhenIngredientNotFound() {
        SupplyOrderItemDto itemDto = new SupplyOrderItemDto();
        itemDto.setIngredientId(999L);

        SupplyOrderDto dto = new SupplyOrderDto();
        dto.setSupplierId(1L);
        dto.setItems(Collections.singletonList(itemDto));

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createSupplyOrder(dto));
    }

    @Test
    void updateSupplyOrderStatus_ShouldUpdateStatus() {
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(testSupplyOrder));
        when(supplyOrderRepository.save(any(SupplyOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(supplyOrderItemRepository.findBySupplyOrderId(anyLong())).thenReturn(Collections.singletonList(testSupplyOrderItem));

        SupplyOrderDto result = service.updateSupplyOrderStatus(1L, SupplyOrderStatus.CONFIRMED);

        assertNotNull(result);
        verify(supplyOrderRepository).save(any(SupplyOrder.class));
    }

    @Test
    void updateSupplyOrderStatus_ToDelivered_ShouldUpdateInventory() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .ingredient(testIngredient)
                .quantity(new BigDecimal("100"))
                .minQuantity(new BigDecimal("10"))
                .maxQuantity(new BigDecimal("500"))
                .build();

        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(testSupplyOrder));
        when(supplyOrderRepository.save(any(SupplyOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(supplyOrderItemRepository.findBySupplyOrderId(1L)).thenReturn(Collections.singletonList(testSupplyOrderItem));
        when(inventoryRepository.findByIngredientId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplyOrderDto result = service.updateSupplyOrderStatus(1L, SupplyOrderStatus.DELIVERED);

        assertNotNull(result);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void updateSupplyOrderStatus_ShouldThrow_WhenNotFound() {
        when(supplyOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateSupplyOrderStatus(999L, SupplyOrderStatus.CONFIRMED));
    }

    @Test
    void deleteSupplyOrder_ShouldDelete() {
        when(supplyOrderRepository.existsById(1L)).thenReturn(true);
        doNothing().when(supplyOrderRepository).deleteById(1L);

        assertDoesNotThrow(() -> service.deleteSupplyOrder(1L));
        verify(supplyOrderRepository).deleteById(1L);
    }

    @Test
    void deleteSupplyOrder_ShouldThrow_WhenNotFound() {
        when(supplyOrderRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteSupplyOrder(999L));
    }
}
