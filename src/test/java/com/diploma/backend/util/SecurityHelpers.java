package com.diploma.backend.util;

import com.diploma.backend.dto.CreateGroupRequest;
import com.diploma.backend.dto.LoginRequest;
import com.diploma.backend.dto.RegisterRequest;
import com.diploma.backend.entity.Role;
import com.diploma.backend.entity.User;
import com.diploma.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

import static java.util.Set.of;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Утилиты для тестов: регистрация пользователя с нужной ролью и получение токена,
 * а также создание тестовых групп.
 */
@Component
public class SecurityHelpers {

    private final MockMvc mockMvc;
    private final ObjectMapper om;
    private final UserRepository userRepo;

    public SecurityHelpers(MockMvc mockMvc, ObjectMapper om, UserRepository userRepo) {
        this.mockMvc = mockMvc;
        this.om = om;
        this.userRepo = userRepo;
    }

    /**
     * Регистрирует пользователя, при необходимости задаёт роль и возвращает access-токен.
     */
    public String token(String email, String password, Role role) throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail(email);
        reg.setPassword(password);
        reg.setFirstName("T");
        reg.setLastName("T");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Set role for all users
            User u = userRepo.findByEmail(email).orElseThrow();
        u.setRoles(new HashSet<>(Set.of(role)));
            userRepo.save(u);

        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword(password);

        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = om.readTree(res.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    /**
     * Создаёт группу под STAFF и добавляет туда студента, возвращает её id.
     */
    public UUID createTestGroupAndGetId(String staffToken, String studentEmail) throws Exception {
        User student = userRepo.findByEmail(studentEmail).orElseThrow();
        CreateGroupRequest req = new CreateGroupRequest();
        req.setName("grp-" + rnd());
        // используем Set вместо List
        req.setMemberIds(of(student.getId()));

        MvcResult res = mockMvc.perform(post("/api/groups")
                        .header("Authorization", bearer(staffToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = om.readTree(res.getResponse().getContentAsString());
        return UUID.fromString(json.get("id").asText());
    }

    /**
     * Заголовок Authorization: Bearer …
     */
    public String bearer(String token) {
        return "Bearer " + token;
    }

    /**
     * Случайная строка длиной 8 для email/названий
     */
    public static String rnd() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
