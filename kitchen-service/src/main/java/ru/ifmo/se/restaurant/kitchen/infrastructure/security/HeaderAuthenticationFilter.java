package ru.ifmo.se.restaurant.kitchen.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userIdHeader = request.getHeader("X-User-Id");
        String usernameHeader = request.getHeader("X-User-Name");
        String roleHeader = request.getHeader("X-User-Role");
        String employeeIdHeader = request.getHeader("X-Employee-Id");

        if (userIdHeader != null && roleHeader != null) {
            if (!hasAccess(roleHeader)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access denied");
                return;
            }

            UserContext userContext = new UserContext();
            userContext.setUserId(Long.parseLong(userIdHeader));
            userContext.setUsername(usernameHeader);
            userContext.setRole(roleHeader);
            if (employeeIdHeader != null && !employeeIdHeader.isEmpty()) {
                userContext.setEmployeeId(Long.parseLong(employeeIdHeader));
            }

            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleHeader));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userContext, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("userContext", userContext);
        }

        filterChain.doFilter(request, response);
    }

    private boolean hasAccess(String role) {
        return "CHEF".equals(role) || "MANAGER".equals(role) || "ADMIN".equals(role);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // Public paths
        if (path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/webjars")) {
            return true;
        }

        // Internal service-to-service calls (POST to /api/kitchen/queue)
        if ("POST".equals(method) && path.startsWith("/api/kitchen/queue")) {
            return true;
        }

        return false;
    }
}
