package com.diploma.backend.config;

import com.diploma.backend.entity.Role;
import com.diploma.backend.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.diploma.backend.security.RateLimitFilter;
import com.diploma.backend.security.RateLimiter;
import com.diploma.backend.security.JwtAuthenticationFilter;
import com.diploma.backend.security.JwtTokenProvider;
import com.diploma.backend.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter, RateLimitFilter rateLimitFilter) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // User management – ADMIN only
                .requestMatchers("/api/users/**").hasRole("ADMIN")

                // Attachments – upload/download/delete
                .requestMatchers(HttpMethod.POST,   "/api/attachments/**").authenticated()
                .requestMatchers(HttpMethod.GET,    "/api/attachments/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/attachments/**").authenticated()

                // Comments – any authenticated user
                .requestMatchers("/api/tasks/*/comments/**").authenticated()
                .requestMatchers("/api/projects/*/comments/**").authenticated()
                .requestMatchers("/api/comments/**").authenticated()

                // Authentication endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // Tasks CRUD - same rules as production
                .requestMatchers(HttpMethod.POST,   "/api/tasks/**").hasAnyRole("ADMIN", "STAFF", "TEACHER")
                .requestMatchers(HttpMethod.GET,    "/api/tasks/**").authenticated()
                .requestMatchers(HttpMethod.PUT,    "/api/tasks/**").hasAnyRole("ADMIN", "STAFF", "TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/tasks/**").hasAnyRole("ADMIN", "STAFF")

                // Reports - based on tests
                .requestMatchers(HttpMethod.POST, "/api/reports").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/reports/user/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/reports/pending").hasRole("ADMIN")
                
                // Schedule - based on tests
                .requestMatchers(HttpMethod.POST, "/api/schedule").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers(HttpMethod.PUT, "/api/schedule/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/schedule/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers(HttpMethod.GET, "/api/schedule/**").authenticated()
                
                // Statistics - based on tests
                .requestMatchers("/api/statistics/**").hasAnyRole("ADMIN", "TEACHER")
                
                // Projects CRUD
                .requestMatchers(HttpMethod.POST,   "/api/projects/**").hasAnyRole("ADMIN", "STAFF", "TEACHER")
                .requestMatchers(HttpMethod.GET,    "/api/projects/**").authenticated()
                .requestMatchers(HttpMethod.PUT,    "/api/projects/**").hasAnyRole("ADMIN", "STAFF", "TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/projects/**").hasAnyRole("ADMIN", "STAFF")

                // Groups CRUD
                .requestMatchers(HttpMethod.POST,   "/api/groups/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.GET,    "/api/groups/**").authenticated()
                .requestMatchers(HttpMethod.PUT,    "/api/groups/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.DELETE, "/api/groups/**").hasAnyRole("ADMIN", "STAFF")

                // Resources endpoint
                .requestMatchers(HttpMethod.POST,   "/api/resources/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers(HttpMethod.GET,    "/api/resources/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/resources/**").hasAnyRole("STAFF", "ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RateLimiter rateLimiter() {
        RateLimiter rateLimiter = new RateLimiter(1000, 100);
        rateLimiter.clearBuckets(); // Очищаем существующие бакеты
        return rateLimiter;
    }

    @Bean
    public RateLimitFilter rateLimitFilter(RateLimiter rateLimiter, ObjectMapper objectMapper) {
        return new RateLimitFilter(rateLimiter, objectMapper);
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider(UserRepository userRepository) {
        return new JwtTokenProvider(userRepository);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }
} 
