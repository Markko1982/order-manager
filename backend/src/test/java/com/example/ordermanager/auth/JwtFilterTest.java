package com.example.ordermanager.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_withoutAuthorizationHeader_doesNotAuthenticateAndContinuesChain()
            throws ServletException, IOException {

        JwtFilter jwtFilter = new JwtFilter(tokenService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenService, userDetailsService);
    }

    @Test
    void doFilterInternal_withAuthorizationHeaderWithoutBearerPrefix_doesNotAuthenticateAndContinuesChain()
            throws ServletException, IOException {

        JwtFilter jwtFilter = new JwtFilter(tokenService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenService, userDetailsService);
    }

    @Test
    void doFilterInternal_withValidBearerTokenAndEmptyContext_authenticatesUserAndContinuesChain()
            throws ServletException, IOException {

        JwtFilter jwtFilter = new JwtFilter(tokenService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails user = org.springframework.security.core.userdetails.User
                .withUsername("user@example.com")
                .password("encoded-password")
                .authorities(List.of())
                .build();

        when(tokenService.getEmailFromToken("valid-token")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(user);

        jwtFilter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertSame(user, authentication.getPrincipal());
        verify(filterChain).doFilter(request, response);
        verify(tokenService).getEmailFromToken("valid-token");
        verify(userDetailsService).loadUserByUsername("user@example.com");
    }

    @Test
    void doFilterInternal_withExistingAuthentication_doesNotOverrideContext()
            throws ServletException, IOException {

        JwtFilter jwtFilter = new JwtFilter(tokenService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        var existingAuthentication = new UsernamePasswordAuthenticationToken(
                "existing-user",
                null,
                List.of());

        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        when(tokenService.getEmailFromToken("valid-token")).thenReturn("user@example.com");

        jwtFilter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        assertSame(existingAuthentication, authentication);
        verify(filterChain).doFilter(request, response);
        verify(tokenService).getEmailFromToken("valid-token");
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_withBearerTokenAndNullUsername_doesNotAuthenticateAndContinuesChain()
            throws ServletException, IOException {

        JwtFilter jwtFilter = new JwtFilter(tokenService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenService.getEmailFromToken("valid-token")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(tokenService).getEmailFromToken("valid-token");
        verifyNoInteractions(userDetailsService);
    }
}