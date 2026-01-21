package ru.ifmo.se.restaurant.billing.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeaderAuthenticationFilterTest {

    private HeaderAuthenticationFilter filter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new HeaderAuthenticationFilter();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenHeadersPresent() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "admin@test.com");
        request.addHeader("X-User-Role", "ADMIN");
        request.setMethod("GET");
        request.setServletPath("/api/bills");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldAllowWaiterForGetRequests() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "WAITER");
        request.setMethod("GET");
        request.setServletPath("/api/bills");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldDenyChefAccess() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "CHEF");
        request.setMethod("GET");
        request.setServletPath("/api/bills");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_ShouldDenyDeleteForNonAdmin() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "WAITER");
        request.setMethod("DELETE");
        request.setServletPath("/api/bills/1");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void doFilterInternal_ShouldAllowDeleteForAdmin() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "ADMIN");
        request.setMethod("DELETE");
        request.setServletPath("/api/bills/1");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldAllowDiscountForManager() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "MANAGER");
        request.setMethod("PATCH");
        request.setServletPath("/api/bills/1/discount");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldDenyDiscountForWaiter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "WAITER");
        request.setMethod("PATCH");
        request.setServletPath("/api/bills/1/discount");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void doFilterInternal_ShouldSetEmployeeId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "ADMIN");
        request.addHeader("X-Employee-Id", "100");
        request.setMethod("GET");
        request.setServletPath("/api/bills");

        filter.doFilterInternal(request, response, filterChain);

        UserContext userContext = (UserContext) request.getAttribute("userContext");
        assertNotNull(userContext);
        assertEquals(100L, userContext.getEmployeeId());
    }

    @Test
    void shouldNotFilter_ShouldReturnTrue_ForActuator() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_ShouldReturnTrue_ForSwagger() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/swagger-ui/index.html");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_ShouldReturnFalse_ForApiPaths() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/bills");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_ShouldProceed_WhenNoHeaders() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setServletPath("/api/bills");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
