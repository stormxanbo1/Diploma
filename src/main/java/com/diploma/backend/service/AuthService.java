package com.diploma.backend.service;

import com.diploma.backend.dto.AuthResponse;
import com.diploma.backend.dto.LoginRequest;
import com.diploma.backend.dto.RegisterRequest;
import com.diploma.backend.entity.Role;
import com.diploma.backend.entity.User;
import com.diploma.backend.repository.UserRepository;
import com.diploma.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /** Регистрация нового пользователя: по умолчанию STUDENT */
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("EMAIL_DUPLICATE");
        }

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .roles(Set.of(Role.STUDENT))              // ← дефолтная роль
                .build();

        user = userRepo.save(user);

        UUID userId = user.getId();
        String access  = tokenProvider.generateAccessToken(userId);
        String refresh = tokenProvider.generateRefreshToken(userId);

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }

    /** Логин по email + password */
    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        UUID userId = user.getId();
        String access  = tokenProvider.generateAccessToken(userId);
        String refresh = tokenProvider.generateRefreshToken(userId);

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }

    /** Обновление access-токена по refresh-токену */
    public AuthResponse refresh(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        UUID userId = tokenProvider.getUserIdFromToken(refreshToken);
        String access = tokenProvider.generateAccessToken(userId);

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refreshToken) // можно реиспользовать
                .build();
    }
}
