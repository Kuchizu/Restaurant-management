package ru.ifmo.se.restaurant.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.SupplierDto;
import ru.ifmo.se.restaurant.dto.SupplyOrderDto;
import ru.ifmo.se.restaurant.dto.SupplyOrderIngredientDto;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.model.entity.Ingredient;
import ru.ifmo.se.restaurant.repository.IngredientRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupplierServiceTest extends BaseIntegrationTest {
    @Autowired
    private SupplierService supplierService;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Test
    void testCreateAndGetSupplier() {
        SupplierDto supplier = new SupplierDto();
        supplier.setName("Test Supplier");
        supplier.setAddress("Test Address");
        supplier.setPhone("123456789");
        supplier.setEmail("test@example.com");

        SupplierDto created = supplierService.createSupplier(supplier);
        assertNotNull(created.getId());
        assertEquals("Test Supplier", created.getName());

        SupplierDto found = supplierService.getSupplierById(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void testGetAllSuppliers() {
        SupplierDto supplier1 = new SupplierDto();
        supplier1.setName("Supplier 1");
        supplier1.setAddress("Address 1");
        supplier1.setPhone("111");
        supplier1.setEmail("s1@example.com");
        supplierService.createSupplier(supplier1);

        SupplierDto supplier2 = new SupplierDto();
        supplier2.setName("Supplier 2");
        supplier2.setAddress("Address 2");
        supplier2.setPhone("222");
        supplier2.setEmail("s2@example.com");
        supplierService.createSupplier(supplier2);

        Page<SupplierDto> suppliers = supplierService.getAllSuppliers(0, 10);
        assertTrue(suppliers.getTotalElements() >= 2);
    }

    @Test
    void testUpdateSupplier() {
        SupplierDto supplier = new SupplierDto();
        supplier.setName("Original Name");
        supplier.setAddress("Original Address");
        supplier.setPhone("123");
        supplier.setEmail("original@example.com");
        SupplierDto created = supplierService.createSupplier(supplier);

        created.setName("Updated Name");
        SupplierDto updated = supplierService.updateSupplier(created.getId(), created);
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void testDeleteSupplier() {
        SupplierDto supplier = new SupplierDto();
        supplier.setName("To Delete");
        supplier.setAddress("Delete Address");
        supplier.setPhone("999");
        supplier.setEmail("delete@example.com");
        SupplierDto created = supplierService.createSupplier(supplier);

        supplierService.deleteSupplier(created.getId());
        // Should not throw, but should be inactive
        assertDoesNotThrow(() -> supplierService.getSupplierById(created.getId()));
    }

    @Test
    void testCreateSupplyOrder() {
        SupplierDto supplier = new SupplierDto();
        supplier.setName("Supplier for Order");
        supplier.setAddress("Order Address");
        supplier.setPhone("555");
        supplier.setEmail("order@example.com");
        SupplierDto createdSupplier = supplierService.createSupplier(supplier);

        Ingredient ingredient = new Ingredient();
        ingredient.setName("Test Ingredient");
        ingredient.setUnit("kg");
        Ingredient savedIngredient = ingredientRepository.save(ingredient);

        SupplyOrderDto order = new SupplyOrderDto();
        order.setSupplierId(createdSupplier.getId());
        order.setNotes("Test order");
        SupplyOrderIngredientDto orderIngredient = new SupplyOrderIngredientDto();
        orderIngredient.setIngredientId(savedIngredient.getId());
        orderIngredient.setQuantity(10);
        orderIngredient.setPricePerUnit(new BigDecimal("5.0"));
        List<SupplyOrderIngredientDto> ingredients = new ArrayList<>();
        ingredients.add(orderIngredient);
        order.setIngredients(ingredients);

        SupplyOrderDto created = supplierService.createSupplyOrder(order);
        assertNotNull(created.getId());
    }

    @Test
    void testGetSupplierByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
            supplierService.getSupplierById(99999L));
    }

    @Test
    void testUpdateSupplierNotFound() {
        SupplierDto dto = new SupplierDto();
        dto.setName("Test");
        assertThrows(ResourceNotFoundException.class, () ->
            supplierService.updateSupplier(99999L, dto));
    }

    @Test
    void testDeleteSupplierNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
            supplierService.deleteSupplier(99999L));
    }

    @Test
    void testGetSupplyOrderById() {
        SupplierDto supplier = new SupplierDto();
        supplier.setName("Test Supplier");
        supplier.setAddress("Address");
        supplier.setPhone("123");
        supplier.setEmail("test@test.com");
        SupplierDto createdSupplier = supplierService.createSupplier(supplier);

        Ingredient ingredient = new Ingredient();
        ingredient.setName("Test Ingredient");
        ingredient.setUnit("kg");
        Ingredient savedIngredient = ingredientRepository.save(ingredient);

        SupplyOrderDto order = new SupplyOrderDto();
        order.setSupplierId(createdSupplier.getId());
        order.setNotes("Test order");
        SupplyOrderIngredientDto orderIngredient = new SupplyOrderIngredientDto();
        orderIngredient.setIngredientId(savedIngredient.getId());
        orderIngredient.setQuantity(10);
        orderIngredient.setPricePerUnit(new BigDecimal("5.0"));
        order.setIngredients(List.of(orderIngredient));
        SupplyOrderDto created = supplierService.createSupplyOrder(order);

        SupplyOrderDto found = supplierService.getSupplyOrderById(created.getId());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void testGetSupplyOrderByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
            supplierService.getSupplyOrderById(99999L));
    }

    @Test
    void testGetAllSupplyOrders() {
        SupplierDto supplier = new SupplierDto();
        supplier.setName("Test Supplier");
        supplier.setAddress("Address");
        supplier.setPhone("123");
        supplier.setEmail("test@test.com");
        SupplierDto createdSupplier = supplierService.createSupplier(supplier);

        Ingredient ingredient = new Ingredient();
        ingredient.setName("Test Ingredient");
        ingredient.setUnit("kg");
        Ingredient savedIngredient = ingredientRepository.save(ingredient);

        SupplyOrderDto order = new SupplyOrderDto();
        order.setSupplierId(createdSupplier.getId());
        order.setNotes("Test order");
        SupplyOrderIngredientDto orderIngredient = new SupplyOrderIngredientDto();
        orderIngredient.setIngredientId(savedIngredient.getId());
        orderIngredient.setQuantity(10);
        orderIngredient.setPricePerUnit(new BigDecimal("5.0"));
        order.setIngredients(List.of(orderIngredient));
        supplierService.createSupplyOrder(order);

        Page<SupplyOrderDto> orders = supplierService.getAllSupplyOrders(0, 10);
        assertNotNull(orders);
        assertTrue(orders.getTotalElements() >= 1);
    }

    @Test
    void testCreateSupplyOrderWithInvalidSupplier() {
        SupplyOrderDto order = new SupplyOrderDto();
        order.setSupplierId(99999L);
        order.setNotes("Test order");

        assertThrows(ResourceNotFoundException.class, () ->
            supplierService.createSupplyOrder(order));
    }

    @Test
    void testCreateSupplyOrderWithInvalidIngredient() {
        SupplierDto supplier = new SupplierDto();
        supplier.setName("Test Supplier");
        supplier.setAddress("Address");
        supplier.setPhone("123");
        supplier.setEmail("test@test.com");
        SupplierDto createdSupplier = supplierService.createSupplier(supplier);

        SupplyOrderDto order = new SupplyOrderDto();
        order.setSupplierId(createdSupplier.getId());
        order.setNotes("Test order");
        SupplyOrderIngredientDto orderIngredient = new SupplyOrderIngredientDto();
        orderIngredient.setIngredientId(99999L);
        orderIngredient.setQuantity(10);
        orderIngredient.setPricePerUnit(new BigDecimal("5.0"));
        order.setIngredients(List.of(orderIngredient));

        assertThrows(ResourceNotFoundException.class, () ->
            supplierService.createSupplyOrder(order));
    }

    @Test
    void testCreateSupplyOrderWithEmptyIngredients() {
        SupplierDto supplier = new SupplierDto();
        supplier.setName("Test Supplier");
        supplier.setAddress("Address");
        supplier.setPhone("123");
        supplier.setEmail("test@test.com");
        SupplierDto createdSupplier = supplierService.createSupplier(supplier);

        SupplyOrderDto order = new SupplyOrderDto();
        order.setSupplierId(createdSupplier.getId());
        order.setNotes("Test order");
        order.setIngredients(new ArrayList<>());

        SupplyOrderDto created = supplierService.createSupplyOrder(order);
        assertNotNull(created.getId());
        assertTrue(created.getIngredients().isEmpty());
    }
}

