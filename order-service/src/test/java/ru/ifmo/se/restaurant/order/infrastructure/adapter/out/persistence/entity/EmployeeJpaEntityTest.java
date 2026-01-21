package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.order.domain.valueobject.EmployeeRole;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeJpaEntityTest {

    @Test
    void constructor_ShouldSetAllFields() {
        EmployeeJpaEntity entity = new EmployeeJpaEntity(
                1L, "John", "Doe", "john@test.com", "+1234567890", EmployeeRole.WAITER
        );

        assertEquals(1L, entity.getId());
        assertEquals("John", entity.getFirstName());
        assertEquals("Doe", entity.getLastName());
        assertEquals("john@test.com", entity.getEmail());
        assertEquals("+1234567890", entity.getPhone());
        assertEquals(EmployeeRole.WAITER, entity.getRole());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        EmployeeJpaEntity entity = new EmployeeJpaEntity();
        entity.setId(1L);
        entity.setFirstName("Jane");
        entity.setLastName("Smith");
        entity.setEmail("jane@test.com");
        entity.setPhone("+0987654321");
        entity.setRole(EmployeeRole.CHEF);

        assertEquals(1L, entity.getId());
        assertEquals("Jane", entity.getFirstName());
        assertEquals("Smith", entity.getLastName());
        assertEquals("jane@test.com", entity.getEmail());
        assertEquals("+0987654321", entity.getPhone());
        assertEquals(EmployeeRole.CHEF, entity.getRole());
    }

    @Test
    void defaultConstructor_ShouldCreateEmptyEntity() {
        EmployeeJpaEntity entity = new EmployeeJpaEntity();
        assertNull(entity.getId());
        assertNull(entity.getFirstName());
        assertNull(entity.getRole());
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        EmployeeJpaEntity entity1 = new EmployeeJpaEntity(
                1L, "John", "Doe", "john@test.com", "+123", EmployeeRole.WAITER
        );
        EmployeeJpaEntity entity2 = new EmployeeJpaEntity(
                1L, "John", "Doe", "john@test.com", "+123", EmployeeRole.WAITER
        );

        assertEquals(entity1, entity2);
    }
}
