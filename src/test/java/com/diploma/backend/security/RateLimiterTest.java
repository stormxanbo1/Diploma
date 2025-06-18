package com.diploma.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    private RateLimiter rateLimiter;
    private static final String TEST_IP = "127.0.0.1";
    private static final String API_ENDPOINT = "/api/tasks";
    private static final String AUTH_ENDPOINT = "/api/auth/login";

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter(30, 5);
    }

    @Test
    @DisplayName("Regular endpoint allows requests within limit")
    void regularEndpointWithinLimit() {
        // Should allow 30 requests (default limit)
        for (int i = 0; i < 30; i++) {
            assertTrue(rateLimiter.tryConsume(TEST_IP, API_ENDPOINT), 
                    "Request " + (i + 1) + " should be allowed");
        }
        
        // 31st request should be rejected
        assertFalse(rateLimiter.tryConsume(TEST_IP, API_ENDPOINT), 
                "Request 31 should be rejected");
    }

    @Test
    @DisplayName("Auth endpoint allows requests within limit")
    void authEndpointWithinLimit() {
        // Should allow 5 requests (auth limit)
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.tryConsume(TEST_IP, AUTH_ENDPOINT), 
                    "Auth request " + (i + 1) + " should be allowed");
        }
        
        // 6th request should be rejected
        assertFalse(rateLimiter.tryConsume(TEST_IP, AUTH_ENDPOINT), 
                "Auth request 6 should be rejected");
    }

    @Test
    @DisplayName("Different IPs have separate buckets")
    void differentIpsSeparateBuckets() {
        String ip1 = "192.168.1.1";
        String ip2 = "192.168.1.2";
        
        // Exhaust first IP
        for (int i = 0; i < 30; i++) {
            rateLimiter.tryConsume(ip1, API_ENDPOINT);
        }
        
        // First IP should be exhausted
        assertFalse(rateLimiter.tryConsume(ip1, API_ENDPOINT), 
                "First IP should be exhausted");
        
        // Second IP should still have tokens
        assertTrue(rateLimiter.tryConsume(ip2, API_ENDPOINT), 
                "Second IP should still have tokens");
    }

    @Test
    @DisplayName("Clearing buckets resets limits")
    void clearingBucketsResetsLimits() {
        // Exhaust the bucket
        for (int i = 0; i < 30; i++) {
            rateLimiter.tryConsume(TEST_IP, API_ENDPOINT);
        }
        
        // Verify it's exhausted
        assertFalse(rateLimiter.tryConsume(TEST_IP, API_ENDPOINT), 
                "Bucket should be exhausted");
        
        // Clear buckets
        rateLimiter.clearBuckets();
        
        // Should be able to consume again
        assertTrue(rateLimiter.tryConsume(TEST_IP, API_ENDPOINT), 
                "Bucket should be reset after clearing");
    }
    
    @Test
    @DisplayName("Get remaining tokens returns correct value")
    void getRemainingTokensReturnsCorrectValue() {
        // Initial state
        assertEquals(30, rateLimiter.getRemainingTokens(TEST_IP, API_ENDPOINT),
                "Initial remaining tokens should be 30");
        
        // Consume 10 tokens
        for (int i = 0; i < 10; i++) {
            rateLimiter.tryConsume(TEST_IP, API_ENDPOINT);
        }
        
        // Should have 20 tokens remaining
        assertEquals(20, rateLimiter.getRemainingTokens(TEST_IP, API_ENDPOINT),
                "Should have 20 tokens remaining");
    }
    
    @Test
    @DisplayName("Set custom limit works correctly")
    void setCustomLimitWorksCorrectly() {
        // Set a custom limit for an endpoint
        String customEndpoint = "/api/custom";
        int customLimit = 10;
        rateLimiter.setCustomLimit(customEndpoint, customLimit);
        
        // Should allow exactly 10 requests
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.tryConsume(TEST_IP, customEndpoint),
                    "Custom request " + (i + 1) + " should be allowed");
        }
        
        // 11th request should be rejected
        assertFalse(rateLimiter.tryConsume(TEST_IP, customEndpoint),
                "Custom request 11 should be rejected");
    }
} 
