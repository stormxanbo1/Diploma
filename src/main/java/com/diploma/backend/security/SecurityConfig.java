package com.diploma.backend.security;

import com.diploma.backend.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtFilter;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS
                .cors(Customizer.withDefaults())
                // Disable CSRF (stateless API)
                .csrf(AbstractHttpConfigurer::disable)
                // Stateless session management
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // URL-based authorization
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

                        // Swagger/OpenAPI
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html"
                        ).permitAll()

                        // Resources endpoint
                        .requestMatchers(HttpMethod.POST,   "/api/resources/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/resources/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/resources/**").hasAnyRole("STAFF", "ADMIN")

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
                        
                        // Authentication endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Tasks CRUD
                        .requestMatchers(HttpMethod.POST,   "/api/tasks/**").hasAnyRole("ADMIN", "STAFF", "TEACHER")
                        .requestMatchers(HttpMethod.GET,    "/api/tasks/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/tasks/**").hasAnyRole("ADMIN", "STAFF", "TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/tasks/**").hasAnyRole("ADMIN", "STAFF")

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

                        // All other requests require authentication
                        .anyRequest().authenticated()
                );

        // Add filters in correct order
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Allow React frontend
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
