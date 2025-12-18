package ru.ifmo.se.restaurant.inventory;

import ru.ifmo.se.restaurant.inventory.dto.*;
import ru.ifmo.se.restaurant.inventory.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {

    // Ingredient methods
    public static Ingredient createMockIngredient(Long id) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(id);
        ingredient.setName("Test Ingredient " + id);
        ingredient.setUnit("kg");
        ingredient.setDescription("Test ingredient description");
        return ingredient;
    }

    public static IngredientDto createMockIngredientDto(Long id) {
        IngredientDto dto = new IngredientDto();
        dto.setId(id);
        dto.setName("Test Ingredient " + id);
        dto.setUnit("kg");
        dto.setDescription("Test ingredient description");
        return dto;
    }

    // Inventory methods
    public static Inventory createMockInventory(Long id, Ingredient ingredient) {
        Inventory inventory = new Inventory();
        inventory.setId(id);
        inventory.setIngredient(ingredient);
        inventory.setQuantity(new BigDecimal("100.00"));
        inventory.setMinQuantity(new BigDecimal("20.00"));
        inventory.setMaxQuantity(new BigDecimal("200.00"));
        inventory.setLastUpdated(LocalDateTime.now());
        return inventory;
    }

    public static Inventory createMockInventory(Long id) {
        return createMockInventory(id, createMockIngredient(1L));
    }

    public static InventoryDto createMockInventoryDto(Long id) {
        InventoryDto dto = new InventoryDto();
        dto.setId(id);
        dto.setIngredientId(1L);
        dto.setIngredientName("Test Ingredient 1");
        dto.setQuantity(new BigDecimal("100.00"));
        dto.setMinQuantity(new BigDecimal("20.00"));
        dto.setMaxQuantity(new BigDecimal("200.00"));
        dto.setLastUpdated(LocalDateTime.now());
        return dto;
    }

    public static Inventory createLowStockInventory(Long id, Ingredient ingredient) {
        Inventory inventory = new Inventory();
        inventory.setId(id);
        inventory.setIngredient(ingredient);
        inventory.setQuantity(new BigDecimal("15.00"));
        inventory.setMinQuantity(new BigDecimal("20.00"));
        inventory.setMaxQuantity(new BigDecimal("200.00"));
        inventory.setLastUpdated(LocalDateTime.now());
        return inventory;
    }

    // Supplier methods
    public static Supplier createMockSupplier(Long id) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setName("Test Supplier " + id);
        supplier.setContactPerson("John Doe");
        supplier.setPhone("+1234567890");
        supplier.setEmail("supplier" + id + "@test.com");
        supplier.setAddress("123 Test Street");
        return supplier;
    }

    public static SupplierDto createMockSupplierDto(Long id) {
        SupplierDto dto = new SupplierDto();
        dto.setId(id);
        dto.setName("Test Supplier " + id);
        dto.setContactPerson("John Doe");
        dto.setPhone("+1234567890");
        dto.setEmail("supplier" + id + "@test.com");
        dto.setAddress("123 Test Street");
        return dto;
    }

    // SupplyOrder methods
    public static SupplyOrder createMockSupplyOrder(Long id, Supplier supplier) {
        SupplyOrder supplyOrder = new SupplyOrder();
        supplyOrder.setId(id);
        supplyOrder.setSupplier(supplier);
        supplyOrder.setOrderDate(LocalDateTime.now());
        supplyOrder.setStatus(SupplyOrderStatus.PENDING);
        supplyOrder.setTotalCost(new BigDecimal("500.00"));
        supplyOrder.setNotes("Test order notes");
        return supplyOrder;
    }

    public static SupplyOrder createMockSupplyOrder(Long id) {
        return createMockSupplyOrder(id, createMockSupplier(1L));
    }

    public static SupplyOrderDto createMockSupplyOrderDto(Long id) {
        SupplyOrderDto dto = new SupplyOrderDto();
        dto.setId(id);
        dto.setSupplierId(1L);
        dto.setSupplierName("Test Supplier 1");
        dto.setOrderDate(LocalDateTime.now());
        dto.setStatus(SupplyOrderStatus.PENDING);
        dto.setTotalCost(new BigDecimal("500.00"));
        dto.setNotes("Test order notes");
        dto.setItems(new ArrayList<>());
        return dto;
    }

    public static SupplyOrder createSupplyOrderWithStatus(Long id, Supplier supplier, SupplyOrderStatus status) {
        SupplyOrder supplyOrder = createMockSupplyOrder(id, supplier);
        supplyOrder.setStatus(status);
        if (status == SupplyOrderStatus.DELIVERED) {
            supplyOrder.setDeliveryDate(LocalDateTime.now());
        }
        return supplyOrder;
    }

    // SupplyOrderIngredient methods
    public static SupplyOrderIngredient createMockSupplyOrderIngredient(
            Long id,
            SupplyOrder supplyOrder,
            Ingredient ingredient,
            BigDecimal quantity,
            BigDecimal unitPrice
    ) {
        SupplyOrderIngredient item = new SupplyOrderIngredient();
        item.setId(id);
        item.setSupplyOrder(supplyOrder);
        item.setIngredient(ingredient);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        return item;
    }

    public static SupplyOrderIngredient createMockSupplyOrderIngredient(Long id) {
        return createMockSupplyOrderIngredient(
                id,
                createMockSupplyOrder(1L),
                createMockIngredient(1L),
                new BigDecimal("50.00"),
                new BigDecimal("10.00")
        );
    }

    public static SupplyOrderItemDto createMockSupplyOrderItemDto(Long id) {
        SupplyOrderItemDto dto = new SupplyOrderItemDto();
        dto.setId(id);
        dto.setIngredientId(1L);
        dto.setIngredientName("Test Ingredient 1");
        dto.setQuantity(new BigDecimal("50.00"));
        dto.setUnitPrice(new BigDecimal("10.00"));
        return dto;
    }

    // Helper method to create a complete supply order with items
    public static SupplyOrderDto createCompleteSupplyOrderDto(Long orderId, Long supplierId) {
        SupplyOrderDto dto = createMockSupplyOrderDto(orderId);
        dto.setSupplierId(supplierId);

        List<SupplyOrderItemDto> items = new ArrayList<>();
        items.add(createMockSupplyOrderItemDto(1L));
        items.add(createMockSupplyOrderItemDto(2L));
        dto.setItems(items);

        return dto;
    }
}
