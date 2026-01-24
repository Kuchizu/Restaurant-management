package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.inventory.domain.entity.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class SupplierJpaEntityTest {

    @Test
    void fromDomain_ShouldMapAllFields() {
        Supplier domain = Supplier.builder()
                .id(1L)
                .name("Fresh Foods Inc")
                .contactPerson("John Doe")
                .phone("+1234567890")
                .email("john@freshfoods.com")
                .address("123 Main St")
                .build();

        SupplierJpaEntity entity = SupplierJpaEntity.fromDomain(domain);

        assertEquals(1L, entity.getId());
        assertEquals("Fresh Foods Inc", entity.getName());
        assertEquals("John Doe", entity.getContactPerson());
        assertEquals("+1234567890", entity.getPhone());
        assertEquals("john@freshfoods.com", entity.getEmail());
        assertEquals("123 Main St", entity.getAddress());
    }

    @Test
    void toDomain_ShouldMapAllFields() {
        SupplierJpaEntity entity = SupplierJpaEntity.builder()
                .id(1L)
                .name("Fresh Foods Inc")
                .contactPerson("John Doe")
                .phone("+1234567890")
                .email("john@freshfoods.com")
                .address("123 Main St")
                .build();

        Supplier domain = entity.toDomain();

        assertEquals(1L, domain.getId());
        assertEquals("Fresh Foods Inc", domain.getName());
        assertEquals("John Doe", domain.getContactPerson());
        assertEquals("+1234567890", domain.getPhone());
        assertEquals("john@freshfoods.com", domain.getEmail());
        assertEquals("123 Main St", domain.getAddress());
    }

    @Test
    void fromDomainAndToDomain_ShouldBeSymmetric() {
        Supplier original = Supplier.builder()
                .id(5L)
                .name("Organic Farms")
                .contactPerson("Jane Smith")
                .phone("+0987654321")
                .email("jane@organic.com")
                .address("456 Farm Rd")
                .build();

        SupplierJpaEntity entity = SupplierJpaEntity.fromDomain(original);
        Supplier converted = entity.toDomain();

        assertEquals(original.getId(), converted.getId());
        assertEquals(original.getName(), converted.getName());
        assertEquals(original.getContactPerson(), converted.getContactPerson());
        assertEquals(original.getPhone(), converted.getPhone());
        assertEquals(original.getEmail(), converted.getEmail());
        assertEquals(original.getAddress(), converted.getAddress());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        SupplierJpaEntity entity = new SupplierJpaEntity();
        entity.setId(1L);
        entity.setName("Test Supplier");
        entity.setContactPerson("Contact");
        entity.setPhone("123");
        entity.setEmail("test@test.com");
        entity.setAddress("Test Address");

        assertEquals(1L, entity.getId());
        assertEquals("Test Supplier", entity.getName());
        assertEquals("Contact", entity.getContactPerson());
        assertEquals("123", entity.getPhone());
        assertEquals("test@test.com", entity.getEmail());
        assertEquals("Test Address", entity.getAddress());
    }
}
