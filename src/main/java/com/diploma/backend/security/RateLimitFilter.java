package com.diploma.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter implements Ordered {

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip = getClientIP(request);
        String endpoint = request.getRequestURI();

        // Проверяем, если это путь, который должен быть исключен из ограничений
        if (shouldSkipRateLimiting(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Проверяем лимит запросов
        if (rateLimiter.tryConsume(ip, endpoint)) {
            // Добавляем заголовки с информацией о лимитах
            response.setHeader("X-Rate-Limit-Remaining", 
                    String.valueOf(rateLimiter.getRemainingTokens(ip, endpoint)));
            
            filterChain.doFilter(request, response);
        } else {
            // Ошибка превышения лимита
            sendTooManyRequestsResponse(response, ip, endpoint);
        }
    }

    private boolean shouldSkipRateLimiting(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Пропускаем Swagger, статические ресурсы и некоторые другие пути
        return uri.contains("/swagger-ui") || 
               uri.contains("/api-docs") || 
               uri.contains("/error") ||
               uri.contains("/public/") ||
               uri.equals("/") ||
               uri.endsWith(".html") ||
               uri.endsWith(".css") ||
               uri.endsWith(".js") ||
               uri.endsWith(".png") ||
               uri.endsWith(".ico");
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendTooManyRequestsResponse(HttpServletResponse response, String ip, String endpoint) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        body.put("error", "Too Many Requests");
        body.put("message", "Превышен лимит запросов. Повторите позже.");

        log.warn("Rate limit exceeded for IP: {} on endpoint: {}", ip, endpoint);
        objectMapper.writeValue(response.getWriter(), body);
    }
} 