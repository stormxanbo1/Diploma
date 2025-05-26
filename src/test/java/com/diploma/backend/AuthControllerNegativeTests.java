package com.diploma.backend;

import com.diploma.backend.config.TestConfig;
import com.diploma.backend.config.TestSecurityConfig;
import com.diploma.backend.dto.LoginRequest;
import com.diploma.backend.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Негативные сценарии для /auth
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Import({TestConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class AuthControllerNegativeTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;

    @Test
    @DisplayName("Регистрация повторного e-mail → 400")
    void registerDuplicateEmail() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("dup@example.com");
        req.setPassword("pass");
        req.setFirstName("A");
        req.setLastName("B");

        // первая попытка OK
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // вторая — ошибка
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Логин с неверным паролем → 401")
    void loginWrongPassword() throws Exception {
        // регистрируем
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail("badpass@example.com");
        reg.setPassword("good");
        reg.setFirstName("A");
        reg.setLastName("B");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(reg)));

        // логин с другим паролем
        LoginRequest login = new LoginRequest();
        login.setEmail("badpass@example.com");
        login.setPassword("wrong");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh с поддельным токеном → 401")
    void refreshInvalidToken() throws Exception {
        String body = "{\"refreshToken\":\"eyJhbGciOi...fake...\"}";
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
