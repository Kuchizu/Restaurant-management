package ru.ifmo.se.restaurant.billing.security;

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

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new HeaderAuthenticationFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidHeaders_ShouldAuthenticate() throws ServletException, IOException {
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "testuser");
        request.addHeader("X-User-Role", "ADMIN");
        request.setMethod("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        UserContext userContext = (UserContext) request.getAttribute("userContext");
        assertNotNull(userContext);
        assertEquals(1L, userContext.getUserId());
        assertEquals("testuser", userContext.getUsername());
        assertEquals("ADMIN", userContext.getRole());
    }

    @Test
    void doFilterInternal_WithEmployeeId_ShouldSetEmployeeId() throws ServletException, IOException {
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "testuser");
        request.addHeader("X-User-Role", "WAITER");
        request.addHeader("X-Employee-Id", "5");
        request.setMethod("GET");

        filter.doFilterInternal(request, response, filterChain);

        UserContext userContext = (UserContext) request.getAttribute("userContext");
        assertEquals(5L, userContext.getEmployeeId());
    }

    @Test
    void doFilterInternal_WithoutHeaders_ShouldContinueWithoutAuth() throws ServletException, IOException {
        request.setMethod("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_DeleteAsNonAdmin_ShouldReturnForbidden() throws ServletException, IOException {
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "testuser");
        request.addHeader("X-User-Role", "WAITER");
        request.setMethod("DELETE");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_DeleteAsAdmin_ShouldAllow() throws ServletException, IOException {
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "testuser");
        request.addHeader("X-User-Role", "ADMIN");
        request.setMethod("DELETE");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_DiscountPatchAsManager_ShouldAllow() throws ServletException, IOException {
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "testuser");
        request.addHeader("X-User-Role", "MANAGER");
        request.setMethod("PATCH");
        request.setServletPath("/api/bills/1/discount");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_DiscountPatchAsWaiter_ShouldReturnForbidden() throws ServletException, IOException {
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "testuser");
        request.addHeader("X-User-Role", "WAITER");
        request.setMethod("PATCH");
        request.setServletPath("/api/bills/1/discount");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void doFilterInternal_GetAsWaiter_ShouldAllow() throws ServletException, IOException {
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "testuser");
        request.addHeader("X-User-Role", "WAITER");
        request.setMethod("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_HeadRequest_ShouldAllow() throws ServletException, IOException {
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "testuser");
        request.addHeader("X-User-Role", "WAITER");
        request.setMethod("HEAD");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_OptionsRequest_ShouldAllow() throws ServletException, IOException {
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "testuser");
        request.addHeader("X-User-Role", "WAITER");
        request.setMethod("OPTIONS");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_PostAsWaiter_ShouldAllow() throws ServletException, IOException {
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "testuser");
        request.addHeader("X-User-Role", "WAITER");
        request.setMethod("POST");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_GetAsChef_ShouldReturnForbidden() throws ServletException, IOException {
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "testuser");
        request.addHeader("X-User-Role", "CHEF");
        request.setMethod("GET");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void shouldNotFilter_ActuatorPath_ShouldReturnTrue() {
        request.setServletPath("/actuator/health");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_SwaggerPath_ShouldReturnTrue() {
        request.setServletPath("/swagger-ui/index.html");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_ApiDocsPath_ShouldReturnTrue() {
        request.setServletPath("/v3/api-docs");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_WebjarsPath_ShouldReturnTrue() {
        request.setServletPath("/webjars/swagger-ui/swagger-ui.css");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_RegularApiPath_ShouldReturnFalse() {
        request.setServletPath("/api/bills");

        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_EmptyEmployeeId_ShouldNotSetEmployeeId() throws ServletException, IOException {
        request.addHeader("X-User-Id", "1");
        request.addHeader("X-User-Name", "testuser");
        request.addHeader("X-User-Role", "ADMIN");
        request.addHeader("X-Employee-Id", "");
        request.setMethod("GET");

        filter.doFilterInternal(request, response, filterChain);

        UserContext userContext = (UserContext) request.getAttribute("userContext");
        assertNull(userContext.getEmployeeId());
    }
}
