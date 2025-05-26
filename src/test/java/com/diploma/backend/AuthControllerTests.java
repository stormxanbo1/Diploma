// src/test/java/com/diploma/backend/controller/AuthControllerTests.java
package com.diploma.backend;

import com.diploma.backend.config.TestConfig;
import com.diploma.backend.config.TestSecurityConfig;
import com.diploma.backend.dto.LoginRequest;
import com.diploma.backend.dto.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Import({TestConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Test
    void registerLoginAndRefreshFlow() throws Exception {
        // 1) Register
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail("test@example.com");
        reg.setPassword("pass123");
        reg.setFirstName("Иван");
        reg.setLastName("Петров");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        JsonNode regJson = om.readTree(regResult.getResponse().getContentAsString());
        String refreshToken = regJson.get("refreshToken").asText();

        // 2) Login
        LoginRequest login = new LoginRequest();
        login.setEmail("test@example.com");
        login.setPassword("pass123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        JsonNode loginJson = om.readTree(loginResult.getResponse().getContentAsString());
        String loginRefresh = loginJson.get("refreshToken").asText();

        // Токены из register и login могут отличаться, но должны быть непустыми
        assertThat(loginRefresh).isNotBlank();

        // 3) Refresh
        String body = "{\"refreshToken\":\"" + loginRefresh + "\"}";
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").value(loginRefresh));
    }
}
