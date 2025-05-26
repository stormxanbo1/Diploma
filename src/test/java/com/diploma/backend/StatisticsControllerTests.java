package com.diploma.backend;

import com.diploma.backend.config.TestConfig;
import com.diploma.backend.config.TestSecurityConfig;
import com.diploma.backend.entity.Role;
import com.diploma.backend.entity.StatisticsType;
import com.diploma.backend.util.SecurityHelpers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class StatisticsControllerTests {

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("ADMIN может получить статистику")
    @WithMockUser(roles = "ADMIN")
    void adminCanGetStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/{type}", StatisticsType.ATTENDANCE_RATE))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TEACHER может получить статистику")
    @WithMockUser(roles = "TEACHER")
    void teacherCanGetStatistics() throws Exception {
        String targetId = UUID.randomUUID().toString();
        mockMvc.perform(get("/api/statistics/{type}/{targetId}", StatisticsType.ATTENDANCE_RATE, targetId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("STUDENT не может получить статистику")
    @WithMockUser(roles = "STUDENT")
    void studentCannotGetStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/{type}", StatisticsType.ATTENDANCE_RATE))
                .andExpect(status().isForbidden());
    }
} 