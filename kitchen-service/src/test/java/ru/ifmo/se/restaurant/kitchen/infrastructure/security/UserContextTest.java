package ru.ifmo.se.restaurant.kitchen.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserContextTest {

    @Test
    void hasRole_ShouldReturnTrue_WhenRoleMatches() {
        UserContext context = new UserContext(1L, "user", "ADMIN", null);

        assertTrue(context.hasRole("ADMIN"));
        assertTrue(context.hasRole("USER", "ADMIN"));
    }

    @Test
    void hasRole_ShouldReturnFalse_WhenRoleDoesNotMatch() {
        UserContext context = new UserContext(1L, "user", "WAITER", null);

        assertFalse(context.hasRole("ADMIN"));
        assertFalse(context.hasRole("MANAGER", "CHEF"));
    }

    @Test
    void isAdmin_ShouldReturnTrue_WhenRoleIsAdmin() {
        UserContext context = new UserContext(1L, "admin", "ADMIN", null);

        assertTrue(context.isAdmin());
    }

    @Test
    void isAdmin_ShouldReturnFalse_WhenRoleIsNotAdmin() {
        UserContext context = new UserContext(1L, "user", "WAITER", null);

        assertFalse(context.isAdmin());
    }

    @Test
    void isManager_ShouldReturnTrue_WhenRoleIsManager() {
        UserContext context = new UserContext(1L, "manager", "MANAGER", null);

        assertTrue(context.isManager());
    }

    @Test
    void isManager_ShouldReturnTrue_WhenRoleIsAdmin() {
        UserContext context = new UserContext(1L, "admin", "ADMIN", null);

        assertTrue(context.isManager());
    }

    @Test
    void isManager_ShouldReturnFalse_WhenRoleIsWaiter() {
        UserContext context = new UserContext(1L, "waiter", "WAITER", null);

        assertFalse(context.isManager());
    }

    @Test
    void isWaiter_ShouldReturnTrue_WhenRoleIsWaiter() {
        UserContext context = new UserContext(1L, "waiter", "WAITER", null);

        assertTrue(context.isWaiter());
    }

    @Test
    void isWaiter_ShouldReturnFalse_WhenRoleIsNotWaiter() {
        UserContext context = new UserContext(1L, "admin", "ADMIN", null);

        assertFalse(context.isWaiter());
    }

    @Test
    void isChef_ShouldReturnTrue_WhenRoleIsChef() {
        UserContext context = new UserContext(1L, "chef", "CHEF", null);

        assertTrue(context.isChef());
    }

    @Test
    void isChef_ShouldReturnFalse_WhenRoleIsNotChef() {
        UserContext context = new UserContext(1L, "admin", "ADMIN", null);

        assertFalse(context.isChef());
    }

    @Test
    void gettersAndSetters_ShouldWork() {
        UserContext context = new UserContext();
        context.setUserId(1L);
        context.setUsername("testuser");
        context.setRole("ADMIN");
        context.setEmployeeId(100L);

        assertEquals(1L, context.getUserId());
        assertEquals("testuser", context.getUsername());
        assertEquals("ADMIN", context.getRole());
        assertEquals(100L, context.getEmployeeId());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyContext() {
        UserContext context = new UserContext();

        assertNull(context.getUserId());
        assertNull(context.getUsername());
        assertNull(context.getRole());
        assertNull(context.getEmployeeId());
    }

    @Test
    void allArgsConstructor_ShouldSetAllFields() {
        UserContext context = new UserContext(1L, "user", "CHEF", 50L);

        assertEquals(1L, context.getUserId());
        assertEquals("user", context.getUsername());
        assertEquals("CHEF", context.getRole());
        assertEquals(50L, context.getEmployeeId());
    }
}
