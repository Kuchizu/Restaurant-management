package ru.ifmo.se.restaurant.kitchen.infrastructure.security;

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
        request.addHeader("X-User-Name", "chef@test.com");
        request.addHeader("X-User-Role", "CHEF");
        request.setServletPath("/api/kitchen/queue/1");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldSetEmployeeId_WhenHeaderPresent() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "chef@test.com");
        request.addHeader("X-User-Role", "CHEF");
        request.addHeader("X-Employee-Id", "100");
        request.setServletPath("/api/kitchen/queue/1");

        filter.doFilterInternal(request, response, filterChain);

        UserContext userContext = (UserContext) request.getAttribute("userContext");
        assertNotNull(userContext);
        assertEquals(100L, userContext.getEmployeeId());
    }

    @Test
    void doFilterInternal_ShouldReturn403_WhenRoleNotAllowed() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "waiter@test.com");
        request.addHeader("X-User-Role", "WAITER");
        request.setServletPath("/api/kitchen/queue/1");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_ShouldAllowAdmin() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "ADMIN");
        request.setServletPath("/api/kitchen/queue/1");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldAllowManager() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Role", "MANAGER");
        request.setServletPath("/api/kitchen/queue/1");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldProceed_WhenNoHeaders() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setServletPath("/api/kitchen/queue/1");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
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
    void shouldNotFilter_ShouldReturnTrue_ForApiDocs() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/v3/api-docs");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_ShouldReturnTrue_ForWebjars() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/webjars/something");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_ShouldReturnTrue_ForPostToQueue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setServletPath("/api/kitchen/queue");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_ShouldReturnFalse_ForGetToQueue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setServletPath("/api/kitchen/queue");

        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_ShouldReturnFalse_ForRegularPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setServletPath("/api/kitchen/queue/1/status");

        assertFalse(filter.shouldNotFilter(request));
    }
}
