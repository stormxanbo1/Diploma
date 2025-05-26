package com.diploma.backend;

import com.diploma.backend.config.TestConfig;
import com.diploma.backend.config.TestSecurityConfig;
import com.diploma.backend.dto.CreateUserRequest;
import com.diploma.backend.entity.Role;
import com.diploma.backend.util.SecurityHelpers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CRUD пользователей доступен только ADMIN
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class UserControllerAdminTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;

    @BeforeEach
    void setUp() throws Exception {
        // Настройка не требуется, поскольку мы используем @WithMockUser
    }

    @Test
    @DisplayName("ADMIN может создать пользователя")
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateUser() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("newuser@" + SecurityHelpers.rnd());
        req.setPassword("p");
        req.setFirstName("N");
        req.setLastName("U");

        // ADMIN -> 201
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("STUDENT не может создать пользователя")
    @WithMockUser(roles = "STUDENT")
    void studentCannotCreateUser() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("newuser@" + SecurityHelpers.rnd());
        req.setPassword("p");
        req.setFirstName("N");
        req.setLastName("U");

        // STUDENT -> 403
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
