package com.diploma.backend;

import com.diploma.backend.config.TestConfig;
import com.diploma.backend.config.TestSecurityConfig;
import com.diploma.backend.dto.CreateScheduleEntryRequest;
import com.diploma.backend.entity.LessonType;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class ScheduleControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;
    @Autowired SecurityHelpers helper;

    String adminToken;
    String teacherToken;
    String studentToken;
    UUID groupId;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = helper.token("admin@" + SecurityHelpers.rnd(), "p", Role.ADMIN);
        teacherToken = helper.token("teacher@" + SecurityHelpers.rnd(), "p", Role.TEACHER);
        studentToken = helper.token("student@" + SecurityHelpers.rnd(), "p", Role.STUDENT);
        groupId = UUID.randomUUID();
    }

    @Test
    @DisplayName("ADMIN может создать запись в расписании")
    void adminCanCreateScheduleEntry() throws Exception {
        CreateScheduleEntryRequest request = new CreateScheduleEntryRequest();
        request.setGroupId(groupId);
        request.setTeacherId(UUID.randomUUID());
        request.setSubject("Test Lesson");
        request.setStartTime(LocalDateTime.now());
        request.setEndTime(LocalDateTime.now().plusHours(1));
        request.setLocation("Room 101");
        request.setLessonType(LessonType.LECTURE);

        mockMvc.perform(post("/api/schedule")
                        .header("Authorization", helper.bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("TEACHER может создать запись в расписании")
    void teacherCanCreateScheduleEntry() throws Exception {
        CreateScheduleEntryRequest request = new CreateScheduleEntryRequest();
        request.setGroupId(groupId);
        request.setTeacherId(UUID.randomUUID());
        request.setSubject("Test Lesson");
        request.setStartTime(LocalDateTime.now());
        request.setEndTime(LocalDateTime.now().plusHours(1));
        request.setLocation("Room 101");
        request.setLessonType(LessonType.LECTURE);

        mockMvc.perform(post("/api/schedule")
                        .header("Authorization", helper.bearer(teacherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("STUDENT не может создать запись в расписании")
    void studentCannotCreateScheduleEntry() throws Exception {
        CreateScheduleEntryRequest request = new CreateScheduleEntryRequest();
        request.setGroupId(groupId);
        request.setTeacherId(UUID.randomUUID());
        request.setSubject("Test Lesson");
        request.setStartTime(LocalDateTime.now());
        request.setEndTime(LocalDateTime.now().plusHours(1));
        request.setLocation("Room 101");
        request.setLessonType(LessonType.LECTURE);

        mockMvc.perform(post("/api/schedule")
                        .header("Authorization", helper.bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN и TEACHER могут обновлять записи в расписании")
    void updateScheduleEntry() throws Exception {
        CreateScheduleEntryRequest request = new CreateScheduleEntryRequest();
        request.setSubject("Updated Lesson");
        request.setDescription("Updated Description");
        request.setLessonType(LessonType.PRACTICE);
        request.setStartTime(LocalDateTime.now().plusDays(2));
        request.setEndTime(LocalDateTime.now().plusDays(2).plusHours(2));
        request.setGroupId(groupId);
        request.setTeacherId(UUID.randomUUID());
        request.setLocation("Room 102");

        // ADMIN может обновить запись
        mockMvc.perform(put("/api/schedule/{id}", UUID.randomUUID())
                        .header("Authorization", helper.bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isOk());

        // TEACHER может обновить запись
        mockMvc.perform(put("/api/schedule/{id}", UUID.randomUUID())
                        .header("Authorization", helper.bearer(teacherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isOk());

        // STUDENT не может обновить запись
        mockMvc.perform(put("/api/schedule/{id}", UUID.randomUUID())
                        .header("Authorization", helper.bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN и TEACHER могут удалять записи из расписания")
    void deleteScheduleEntry() throws Exception {
        // ADMIN может удалить запись
        mockMvc.perform(delete("/api/schedule/{id}", UUID.randomUUID())
                        .header("Authorization", helper.bearer(adminToken)))
                .andExpect(status().isNoContent());

        // TEACHER может удалить запись
        mockMvc.perform(delete("/api/schedule/{id}", UUID.randomUUID())
                        .header("Authorization", helper.bearer(teacherToken)))
                .andExpect(status().isNoContent());

        // STUDENT не может удалить запись
        mockMvc.perform(delete("/api/schedule/{id}", UUID.randomUUID())
                        .header("Authorization", helper.bearer(studentToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Все пользователи могут получать записи расписания")
    void getScheduleEntries() throws Exception {
        // ADMIN может получить записи
        mockMvc.perform(get("/api/schedule")
                        .header("Authorization", helper.bearer(adminToken)))
                .andExpect(status().isOk());

        // TEACHER может получить записи
        mockMvc.perform(get("/api/schedule")
                        .header("Authorization", helper.bearer(teacherToken)))
                .andExpect(status().isOk());

        // STUDENT может получить записи
        mockMvc.perform(get("/api/schedule")
                        .header("Authorization", helper.bearer(studentToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Все пользователи могут получать записи расписания по группе")
    void getScheduleEntriesByGroup() throws Exception {
        // ADMIN может получить записи по группе
        mockMvc.perform(get("/api/schedule/group/{groupId}", groupId)
                        .header("Authorization", helper.bearer(adminToken)))
                .andExpect(status().isOk());

        // TEACHER может получить записи по группе
        mockMvc.perform(get("/api/schedule/group/{groupId}", groupId)
                        .header("Authorization", helper.bearer(teacherToken)))
                .andExpect(status().isOk());

        // STUDENT может получить записи по группе
        mockMvc.perform(get("/api/schedule/group/{groupId}", groupId)
                        .header("Authorization", helper.bearer(studentToken)))
                .andExpect(status().isOk());
    }
} 