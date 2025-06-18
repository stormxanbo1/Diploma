package com.diploma.backend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimiter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    // Кастомные лимиты для эндпоинтов
    private final Map<String, Integer> customLimits = new ConcurrentHashMap<>();

    // Лимиты по умолчанию
    private final int defaultRequestsPerMinute;

    // Лимиты для аутентификации
    private final int authRequestsPerMinute;

    /**
     * Создает лимитер с параметрами по умолчанию из конфигурации
     */
    public RateLimiter(
            @Value("${rate.limit.requests-per-minute:30}") int defaultRequestsPerMinute,
            @Value("${rate.limit.auth-requests-per-minute:5}") int authRequestsPerMinute) {
        this.defaultRequestsPerMinute = defaultRequestsPerMinute;
        this.authRequestsPerMinute = authRequestsPerMinute;
        log.info("Rate limiter initialized with {} requests/min default, {} requests/min for auth",
                defaultRequestsPerMinute, authRequestsPerMinute);
    }

    /**
     * Очищает все существующие бакеты - полезно для тестов
     */
    public void clearBuckets() {
        buckets.clear();
        log.info("All rate limiter buckets cleared");
    }

    /**
     * Проверяет, можно ли выполнить запрос для данного IP и эндпоинта
     * Возвращает true, если запрос разрешен
     */
    public boolean tryConsume(String ip, String endpoint) {
        // Создаем ключ для бакета
        String key = ip + ":" + endpoint;
        
        Bucket bucket = buckets.computeIfAbsent(key, k -> {
            int limit = getEndpointLimit(endpoint);
            Bandwidth bandwidth = Bandwidth.classic(limit, Refill.greedy(limit, Duration.ofMinutes(1)));
            return Bucket.builder().addLimit(bandwidth).build();
        });

        boolean allowed = bucket.tryConsume(1);
        if (!allowed) {
            log.warn("Rate limit exceeded for IP: {} on endpoint: {}", ip, endpoint);
        }
        return allowed;
    }

    /**
     * Получает оставшееся количество запросов для IP и эндпоинта
     */
    public long getRemainingTokens(String ip, String endpoint) {
        String key = ip + ":" + endpoint;
        
        Bucket bucket = buckets.get(key);
        if (bucket == null) {
            return getEndpointLimit(endpoint);
        }
        
        return bucket.getAvailableTokens();
    }

    /**
     * Устанавливает новые лимиты для конкретных эндпоинтов (например, для тестов)
     */
    public void setCustomLimit(String endpoint, int requestsPerMinute) {
        log.info("Setting custom rate limit for endpoint {}: {} requests/min", 
                endpoint, requestsPerMinute);
        
        // Сохраняем кастомный лимит
        customLimits.put(endpoint, requestsPerMinute);
        
        // Очищаем бакеты, связанные с этим эндпоинтом
        buckets.keySet().stream()
                .filter(key -> key.endsWith(endpoint))
                .forEach(buckets::remove);
    }
    
    /**
     * Определяет лимит для эндпоинта (кастомный, аутентификация или по умолчанию)
     */
    private int getEndpointLimit(String endpoint) {
        // Проверяем, есть ли кастомный лимит
        if (customLimits.containsKey(endpoint)) {
            return customLimits.get(endpoint);
        }
        
        // Проверяем, является ли эндпоинт эндпоинтом аутентификации
        if (endpoint.startsWith("/api/auth/")) {
            return authRequestsPerMinute;
        }
        
        // Возвращаем лимит по умолчанию
        return defaultRequestsPerMinute;
    }
} 
