package ru.ifmo.se.restaurant.inventory.infrastructure.security;

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
    void doFilterInternal_ShouldAllowAdminGetRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "ADMIN");
        request.setMethod("GET");
        request.setServletPath("/api/inventory");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldAllowChefGetRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "CHEF");
        request.setMethod("GET");
        request.setServletPath("/api/inventory");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldDenyWaiterGetRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "WAITER");
        request.setMethod("GET");
        request.setServletPath("/api/inventory");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void doFilterInternal_ShouldDenyChefPostRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "CHEF");
        request.setMethod("POST");
        request.setServletPath("/api/inventory");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void doFilterInternal_ShouldAllowManagerPostRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "MANAGER");
        request.setMethod("POST");
        request.setServletPath("/api/inventory");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldSetEmployeeId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "ADMIN");
        request.addHeader("X-Employee-Id", "100");
        request.setMethod("GET");
        request.setServletPath("/api/inventory");

        filter.doFilterInternal(request, response, filterChain);

        UserContext userContext = (UserContext) request.getAttribute("userContext");
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
        request.setServletPath("/api/inventory");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_ShouldProceed_WhenNoHeaders() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setServletPath("/api/inventory");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
