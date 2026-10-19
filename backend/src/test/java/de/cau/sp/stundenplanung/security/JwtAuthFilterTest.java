package de.cau.sp.stundenplanung.security;

import de.cau.sp.stundenplanung.model.Rolle;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {
    @Mock JwtService jwtService;
    @InjectMocks JwtAuthFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noBearerTokenLeavesRequestUnauthenticatedAndContinues() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(response.getErrorMessage());
        assertNotNull(chain.getRequest());
        verifyNoInteractions(jwtService);
    }

    @Test
    void validTokenCreatesAuthenticationWithMappedRole() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("mmustermann");
        when(claims.get("role", String.class)).thenReturn("LEHRKRAFT");
        when(jwtService.parseToken("good")).thenReturn(claims);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("mmustermann", SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LEHRKRAFT")));
        assertNotNull(chain.getRequest());
    }

    @Test
    void invalidJwtReturnsUnauthorizedAndDoesNotContinue() throws Exception {
        when(jwtService.parseToken("abc")).thenThrow(new io.jsonwebtoken.JwtException("bad token"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertEquals("Ungültiger Token", response.getErrorMessage());
        assertNull(chain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void unknownRoleInValidJwtReturnsUnauthorized() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("max");
        when(claims.get("role", String.class)).thenReturn("UNKNOWN");
        when(jwtService.parseToken("good")).thenReturn(claims);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertNull(chain.getRequest());
    }
}
