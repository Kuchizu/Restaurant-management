package ru.ifmo.se.restaurant.kitchen.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserContextTest {

    @Test
    void hasRole_WithMatchingRole_ShouldReturnTrue() {
        UserContext context = new UserContext(1L, "user", "CHEF", null);

        assertTrue(context.hasRole("CHEF"));
        assertTrue(context.hasRole("WAITER", "CHEF"));
    }

    @Test
    void hasRole_WithNonMatchingRole_ShouldReturnFalse() {
        UserContext context = new UserContext(1L, "user", "WAITER", null);

        assertFalse(context.hasRole("CHEF"));
        assertFalse(context.hasRole("MANAGER", "ADMIN"));
    }

    @Test
    void isAdmin_WithAdminRole_ShouldReturnTrue() {
        UserContext context = new UserContext(1L, "user", "ADMIN", null);

        assertTrue(context.isAdmin());
    }

    @Test
    void isAdmin_WithNonAdminRole_ShouldReturnFalse() {
        UserContext context = new UserContext(1L, "user", "CHEF", null);

        assertFalse(context.isAdmin());
    }

    @Test
    void isManager_WithManagerRole_ShouldReturnTrue() {
        UserContext context = new UserContext(1L, "user", "MANAGER", null);

        assertTrue(context.isManager());
    }

    @Test
    void isManager_WithAdminRole_ShouldReturnTrue() {
        UserContext context = new UserContext(1L, "user", "ADMIN", null);

        assertTrue(context.isManager());
    }

    @Test
    void isManager_WithChefRole_ShouldReturnFalse() {
        UserContext context = new UserContext(1L, "user", "CHEF", null);

        assertFalse(context.isManager());
    }

    @Test
    void isWaiter_WithWaiterRole_ShouldReturnTrue() {
        UserContext context = new UserContext(1L, "user", "WAITER", null);

        assertTrue(context.isWaiter());
    }

    @Test
    void isWaiter_WithNonWaiterRole_ShouldReturnFalse() {
        UserContext context = new UserContext(1L, "user", "CHEF", null);

        assertFalse(context.isWaiter());
    }

    @Test
    void isChef_WithChefRole_ShouldReturnTrue() {
        UserContext context = new UserContext(1L, "user", "CHEF", null);

        assertTrue(context.isChef());
    }

    @Test
    void isChef_WithNonChefRole_ShouldReturnFalse() {
        UserContext context = new UserContext(1L, "user", "ADMIN", null);

        assertFalse(context.isChef());
    }

    @Test
    void constructor_ShouldSetAllFields() {
        UserContext context = new UserContext(1L, "testuser", "CHEF", 5L);

        assertEquals(1L, context.getUserId());
        assertEquals("testuser", context.getUsername());
        assertEquals("CHEF", context.getRole());
        assertEquals(5L, context.getEmployeeId());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        UserContext context = new UserContext();
        context.setUserId(2L);
        context.setUsername("newuser");
        context.setRole("MANAGER");
        context.setEmployeeId(10L);

        assertEquals(2L, context.getUserId());
        assertEquals("newuser", context.getUsername());
        assertEquals("MANAGER", context.getRole());
        assertEquals(10L, context.getEmployeeId());
    }
}
