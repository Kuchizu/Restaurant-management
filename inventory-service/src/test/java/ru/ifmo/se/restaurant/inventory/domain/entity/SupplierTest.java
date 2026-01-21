package ru.ifmo.se.restaurant.inventory.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupplierTest {

    @Test
    void builder_ShouldCreateSupplier() {
        Supplier supplier = Supplier.builder()
                .id(1L)
                .name("Test Supplier")
                .contactPerson("John Doe")
                .phone("123-456-7890")
                .email("supplier@test.com")
                .address("123 Test Street")
                .build();

        assertEquals(1L, supplier.getId());
        assertEquals("Test Supplier", supplier.getName());
        assertEquals("John Doe", supplier.getContactPerson());
        assertEquals("123-456-7890", supplier.getPhone());
        assertEquals("supplier@test.com", supplier.getEmail());
        assertEquals("123 Test Street", supplier.getAddress());
    }

    @Test
    void updateInfo_ShouldUpdateAllFields() {
        Supplier supplier = Supplier.builder()
                .id(1L)
                .name("Old Name")
                .contactPerson("Old Person")
                .phone("111-111-1111")
                .email("old@test.com")
                .address("Old Address")
                .build();

        Supplier updated = supplier.updateInfo(
                "New Name",
                "New Person",
                "222-222-2222",
                "new@test.com",
                "New Address"
        );

        assertEquals(1L, updated.getId());
        assertEquals("New Name", updated.getName());
        assertEquals("New Person", updated.getContactPerson());
        assertEquals("222-222-2222", updated.getPhone());
        assertEquals("new@test.com", updated.getEmail());
        assertEquals("New Address", updated.getAddress());
    }

    @Test
    void updateInfo_ShouldKeepExistingValues_WhenNewValuesAreNull() {
        Supplier supplier = Supplier.builder()
                .id(1L)
                .name("Original Name")
                .contactPerson("Original Person")
                .phone("123-456-7890")
                .email("original@test.com")
                .address("Original Address")
                .build();

        Supplier updated = supplier.updateInfo(null, null, null, null, null);

        assertEquals("Original Name", updated.getName());
        assertEquals("Original Person", updated.getContactPerson());
        assertEquals("123-456-7890", updated.getPhone());
        assertEquals("original@test.com", updated.getEmail());
        assertEquals("Original Address", updated.getAddress());
    }

    @Test
    void updateInfo_ShouldUpdateOnlyNonNullFields() {
        Supplier supplier = Supplier.builder()
                .id(1L)
                .name("Original Name")
                .contactPerson("Original Person")
                .phone("123-456-7890")
                .email("original@test.com")
                .address("Original Address")
                .build();

        Supplier updated = supplier.updateInfo("New Name", null, "999-999-9999", null, null);

        assertEquals("New Name", updated.getName());
        assertEquals("Original Person", updated.getContactPerson());
        assertEquals("999-999-9999", updated.getPhone());
        assertEquals("original@test.com", updated.getEmail());
        assertEquals("Original Address", updated.getAddress());
    }
}
