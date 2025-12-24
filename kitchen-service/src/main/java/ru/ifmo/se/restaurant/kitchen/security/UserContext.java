package ru.ifmo.se.restaurant.kitchen.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private Long userId;
    private String username;
    private String role;
    private Long employeeId;

    public boolean hasRole(String... roles) {
        for (String r : roles) {
            if (r.equals(this.role)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isManager() {
        return "MANAGER".equals(role) || isAdmin();
    }

    public boolean isWaiter() {
        return "WAITER".equals(role);
    }

    public boolean isChef() {
        return "CHEF".equals(role);
    }
}
