package com.diploma.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter filter;
    private RateLimiter rateLimiter;
    private ObjectMapper objectMapper;
    private FilterChain filterChain;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter(30, 5);
        objectMapper = new ObjectMapper();
        filter = new RateLimitFilter(rateLimiter, objectMapper);
        filterChain = mock(FilterChain.class);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("Regular request within rate limit is processed")
    void regularRequestWithinLimit() throws ServletException, IOException {
        request.setRequestURI("/api/tasks");
        request.setRemoteAddr("127.0.0.1");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Auth request within rate limit is processed")
    void authRequestWithinLimit() throws ServletException, IOException {
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("127.0.0.1");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Request exceeding rate limit is rejected")
    void requestExceedingLimit() throws ServletException, IOException {
        request.setRequestURI("/api/tasks");
        request.setRemoteAddr("127.0.0.1");

        // Exhaust the bucket
        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // Next request should be rejected
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(429, response.getStatus());
        verify(filterChain, times(30)).doFilter(request, response);
    }

    @Test
    @DisplayName("Auth request exceeding rate limit is rejected")
    void authRequestExceedingLimit() throws ServletException, IOException {
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("127.0.0.1");

        // Exhaust the auth bucket
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // Next request should be rejected
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(429, response.getStatus());
        verify(filterChain, times(5)).doFilter(request, response);
    }

    @Test
    @DisplayName("X-Forwarded-For header is used for IP detection")
    void xForwardedForHeaderUsed() throws ServletException, IOException {
        request.setRequestURI("/api/tasks");
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }
} 
