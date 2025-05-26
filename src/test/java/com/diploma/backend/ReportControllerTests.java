package com.diploma.backend;

import com.diploma.backend.config.TestConfig;
import com.diploma.backend.config.TestSecurityConfig;
import com.diploma.backend.dto.CreateReportRequest;
import com.diploma.backend.entity.ReportStatus;
import com.diploma.backend.entity.ReportType;
import com.diploma.backend.entity.Role;
import com.diploma.backend.entity.User;
import com.diploma.backend.repository.UserRepository;
import com.diploma.backend.util.SecurityHelpers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Import({TestConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class ReportControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityHelpers helper;
    
    @Autowired
    private UserRepository userRepository;

    private String adminToken;
    private String teacherToken;
    private String studentToken;
    private UUID adminId;
    private UUID teacherId;
    private UUID studentId;

    @BeforeEach
    void setUp() throws Exception {
        // Создаем пользователей для тестов
        String adminEmail = "admin@" + SecurityHelpers.rnd();
        String teacherEmail = "teacher@" + SecurityHelpers.rnd();
        String studentEmail = "student@" + SecurityHelpers.rnd();
        
        adminToken = helper.token(adminEmail, "p", Role.ADMIN);
        teacherToken = helper.token(teacherEmail, "p", Role.TEACHER);
        studentToken = helper.token(studentEmail, "p", Role.STUDENT);
        
        // Сохраняем UUID пользователей для использования в тестах
        User admin = userRepository.findByEmail(adminEmail).orElseThrow();
        User teacher = userRepository.findByEmail(teacherEmail).orElseThrow();
        User student = userRepository.findByEmail(studentEmail).orElseThrow();
        
        adminId = admin.getId();
        teacherId = teacher.getId();
        studentId = student.getId();
    }

    @Test
    @DisplayName("ADMIN и TEACHER могут создавать отчеты")
    void createReport() throws Exception {
        CreateReportRequest request = new CreateReportRequest();
        request.setType(ReportType.STUDENT_PERFORMANCE);
        request.setStartDate(LocalDateTime.now().minusDays(7));
        request.setEndDate(LocalDateTime.now());
        request.setParameters("{\"studentId\":\"123\"}");

        // ADMIN может создать отчет
        mockMvc.perform(post("/api/reports")
                        .header("Authorization", helper.bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(req -> {
                            req.setRemoteUser(adminId.toString());
                            return req;
                        }))
                .andExpect(status().isCreated());

        // TEACHER может создать отчет
        mockMvc.perform(post("/api/reports")
                        .header("Authorization", helper.bearer(teacherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(req -> {
                            req.setRemoteUser(teacherId.toString());
                            return req;
                        }))
                .andExpect(status().isCreated());

        // STUDENT не может создать отчет
        mockMvc.perform(post("/api/reports")
                        .header("Authorization", helper.bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(req -> {
                            req.setRemoteUser(studentId.toString());
                            return req;
                        }))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Пользователь может получить свои отчеты")
    @WithMockUser(roles = "ADMIN")
    void getUserReportsAsAdmin() throws Exception {
        mockMvc.perform(get("/api/reports/user"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("TEACHER может получить свои отчеты")
    @WithMockUser(roles = "TEACHER")
    void getUserReportsAsTeacher() throws Exception {
        mockMvc.perform(get("/api/reports/user"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("STUDENT не может получить свои отчеты")
    @WithMockUser(roles = "STUDENT")
    void getUserReportsAsStudent() throws Exception {
        mockMvc.perform(get("/api/reports/user"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN может получить свои отчеты по типу")
    @WithMockUser(roles = "ADMIN")
    void getUserReportsByTypeAsAdmin() throws Exception {
        mockMvc.perform(get("/api/reports/user/type/{type}", ReportType.STUDENT_PERFORMANCE))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("TEACHER может получить свои отчеты по типу")
    @WithMockUser(roles = "TEACHER")
    void getUserReportsByTypeAsTeacher() throws Exception {
        mockMvc.perform(get("/api/reports/user/type/{type}", ReportType.STUDENT_PERFORMANCE))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("STUDENT не может получить свои отчеты по типу")
    @WithMockUser(roles = "STUDENT")
    void getUserReportsByTypeAsStudent() throws Exception {
        mockMvc.perform(get("/api/reports/user/type/{type}", ReportType.STUDENT_PERFORMANCE))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN может получить свои отчеты по статусу")
    @WithMockUser(roles = "ADMIN")
    void getUserReportsByStatusAsAdmin() throws Exception {
        mockMvc.perform(get("/api/reports/user/status/{status}", ReportStatus.PENDING))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("TEACHER может получить свои отчеты по статусу")
    @WithMockUser(roles = "TEACHER")
    void getUserReportsByStatusAsTeacher() throws Exception {
        mockMvc.perform(get("/api/reports/user/status/{status}", ReportStatus.PENDING))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("STUDENT не может получить свои отчеты по статусу")
    @WithMockUser(roles = "STUDENT")
    void getUserReportsByStatusAsStudent() throws Exception {
        mockMvc.perform(get("/api/reports/user/status/{status}", ReportStatus.PENDING))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Только ADMIN может получить все ожидающие отчеты")
    @WithMockUser(roles = "ADMIN")
    void getPendingReportsAsAdmin() throws Exception {
        mockMvc.perform(get("/api/reports/pending"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("TEACHER не может получить все ожидающие отчеты")
    @WithMockUser(roles = "TEACHER")
    void getPendingReportsAsTeacher() throws Exception {
        mockMvc.perform(get("/api/reports/pending"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("STUDENT не может получить все ожидающие отчеты")
    @WithMockUser(roles = "STUDENT")
    void getPendingReportsAsStudent() throws Exception {
        mockMvc.perform(get("/api/reports/pending"))
                .andExpect(status().isForbidden());
    }
} 