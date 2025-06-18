package com.diploma.backend.security;

import com.diploma.backend.entity.Role;
import com.diploma.backend.entity.User;
import com.diploma.backend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final int MIN_SECRET_LENGTH = 32;
    private static final int MIN_ACCESS_EXPIRATION_MINUTES = 5;
    private static final int MIN_REFRESH_EXPIRATION_HOURS = 1;

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration-minutes:30}")
    private long accessExpirationMinutes;

    @Value("${jwt.refresh-expiration-hours:24}")
    private long refreshExpirationHours;

    private final ConcurrentMap<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    @PostConstruct
    public void validateConfiguration() {
        if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    String.format("JWT secret должен быть не менее %d символов", MIN_SECRET_LENGTH)
            );
        }
        if (accessExpirationMinutes < MIN_ACCESS_EXPIRATION_MINUTES) {
            throw new IllegalStateException(
                    String.format("Время жизни access токена должно быть не менее %d минут", MIN_ACCESS_EXPIRATION_MINUTES)
            );
        }
        if (refreshExpirationHours < MIN_REFRESH_EXPIRATION_HOURS) {
            throw new IllegalStateException(
                    String.format("Время жизни refresh токена должно быть не менее %d часов", MIN_REFRESH_EXPIRATION_HOURS)
            );
        }
        log.info("JWT configuration validated successfully");
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpirationMinutes * 60_000);
        
        List<String> roles = getUserRoles(userId);
        
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(exp)
                .claim("roles", roles)  // Добавляем роли в токен
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpirationHours * 3_600_000);
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(exp)
                .signWith(getSigningKey())
                .compact();
    }

    private List<String> getUserRoles(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRoles().stream()
                        .map(role -> "ROLE_" + role.name())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public boolean validateToken(String token) {
        try {
            if (isTokenBlacklisted(token)) {
                throw new BadCredentialsException("Token has been revoked");
            }
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("INVALID_JWT", ex);
        }
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return UUID.fromString(claims.getSubject());
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.get("roles", List.class);
    }


    private boolean isTokenBlacklisted(String token) {
        Date expiration = blacklistedTokens.get(token);
        if (expiration != null) {
            if (expiration.before(new Date())) {
                blacklistedTokens.remove(token);
                return false;
            }
            return true;
        }
        return false;
    }

    @PostConstruct
    public void startCleanupTask() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Date now = new Date();
                    blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
                    Thread.sleep(3600000); // Очистка раз в час
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "token-cleanup");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
        log.info("Token cleanup task started");
    }
}
