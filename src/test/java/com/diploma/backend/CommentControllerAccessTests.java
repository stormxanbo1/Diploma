package com.diploma.backend;

import com.diploma.backend.config.TestConfig;
import com.diploma.backend.config.TestSecurityConfig;
import com.diploma.backend.dto.CreateCommentRequest;
import com.diploma.backend.dto.CreateTaskRequest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Автор может удалить свой комментарий, чужой — нельзя; STAFF может
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Import({TestConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class CommentControllerAccessTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;
    @Autowired SecurityHelpers helper;
    @Autowired UserRepository userRepo;

    private String authorEmail;
    private String strangerEmail;
    private String staffEmail;
    private String authorToken;
    private String strangerToken;
    private String staffToken;
    private String taskId;

    @BeforeEach
    void init() throws Exception {
        // Создаем уникальные email для каждого теста
        authorEmail = "auth@" + SecurityHelpers.rnd();
        strangerEmail = "str@" + SecurityHelpers.rnd();
        staffEmail = "staff@" + SecurityHelpers.rnd();
        
        // Получаем токены с правильными ролями
        authorToken = helper.token(authorEmail, "p", Role.TEACHER);
        strangerToken = helper.token(strangerEmail, "p", Role.STUDENT);
        staffToken = helper.token(staffEmail, "p", Role.STAFF);

        // Проверяем роли в БД для всех пользователей
        User author = userRepo.findByEmail(authorEmail).orElseThrow();
        User stranger = userRepo.findByEmail(strangerEmail).orElseThrow();
        User staff = userRepo.findByEmail(staffEmail).orElseThrow();
        
        System.out.println("[DEBUG] Author roles in DB: " + author.getRoles());
        System.out.println("[DEBUG] Stranger roles in DB: " + stranger.getRoles());
        System.out.println("[DEBUG] Staff roles in DB: " + staff.getRoles());
        
        // Убеждаемся, что у пользователей правильные роли
        assertTrue(author.getRoles().contains(Role.TEACHER), "Author should have TEACHER role");
        assertTrue(stranger.getRoles().contains(Role.STUDENT), "Stranger should have STUDENT role");
        assertTrue(staff.getRoles().contains(Role.STAFF), "Staff should have STAFF role");

        // автор создаёт задачу
        CreateTaskRequest req = new CreateTaskRequest();
        req.setTitle("Task with comments " + SecurityHelpers.rnd());
        var taskResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", helper.bearer(authorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
                
        String location = taskResult.getResponse().getHeader("Location");
        assertNotNull(location, "Location header must not be null after task creation");
        taskId = location.substring(location.lastIndexOf('/') + 1);
        System.out.println("[DEBUG] Created task with ID: " + taskId);
    }

    @Test
    @DisplayName("Автор удаляет комментарий → 204, stranger → 403, STAFF → 204")
    void commentDeleteRoles() throws Exception {
        // 1. Автор добавляет комментарий
        CreateCommentRequest cReq = new CreateCommentRequest();
        cReq.setText("Comment by author that will be deleted by author");
        
        MvcResult commentResult = mockMvc.perform(post("/api/tasks/{id}/comments", taskId)
                        .header("Authorization", helper.bearer(authorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(cReq)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value(cReq.getText()))
                .andExpect(jsonPath("$.authorName").exists())
                .andReturn();
                
        String loc = commentResult.getResponse().getHeader("Location");
        assertNotNull(loc, "Location header must not be null after comment creation");
        String commentId = loc.substring(loc.lastIndexOf('/') + 1);
        System.out.println("[DEBUG] Created comment ID for author delete test: " + commentId);

        // Проверяем, что комментарий отображается в списке
        mockMvc.perform(get("/api/tasks/{id}/comments", taskId)
                        .header("Authorization", helper.bearer(authorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + commentId + "')]").exists());

        // 2. Автор удаляет свой комментарий
        mockMvc.perform(delete("/api/comments/{id}", commentId)
                        .header("Authorization", helper.bearer(authorToken)))
                .andExpect(status().isNoContent());

        // Проверяем, что комментарий был удален
        mockMvc.perform(get("/api/tasks/{id}/comments", taskId)
                        .header("Authorization", helper.bearer(authorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + commentId + "')]").doesNotExist());

        // 3. Создаем новый комментарий для тестирования других сценариев
        cReq.setText("Comment by author that will be attempted to be deleted by stranger and staff");
        commentResult = mockMvc.perform(post("/api/tasks/{id}/comments", taskId)
                        .header("Authorization", helper.bearer(authorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(cReq)))
                .andExpect(status().isCreated())
                .andReturn();
                
        loc = commentResult.getResponse().getHeader("Location");
        assertNotNull(loc, "Location header must not be null after second comment creation");
        commentId = loc.substring(loc.lastIndexOf('/') + 1);
        System.out.println("[DEBUG] Created comment ID for stranger/staff test: " + commentId);

        // Проверяем, что комментарий существует
        mockMvc.perform(get("/api/tasks/{id}/comments", taskId)
                        .header("Authorization", helper.bearer(authorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + commentId + "')]").exists());

        // 4. Посторонний пользователь пытается удалить комментарий
        mockMvc.perform(delete("/api/comments/{id}", commentId)
                        .header("Authorization", helper.bearer(strangerToken)))
                .andExpect(status().isForbidden());

        // Проверяем, что комментарий все еще существует после попытки удаления посторонним
        mockMvc.perform(get("/api/tasks/{id}/comments", taskId)
                        .header("Authorization", helper.bearer(authorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + commentId + "')]").exists());

        // 5. STAFF удаляет комментарий
        mockMvc.perform(delete("/api/comments/{id}", commentId)
                        .header("Authorization", helper.bearer(staffToken)))
                .andExpect(status().isNoContent());

        // Проверяем, что комментарий удален
        mockMvc.perform(get("/api/tasks/{id}/comments", taskId)
                        .header("Authorization", helper.bearer(authorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + commentId + "')]").doesNotExist());
    }
}
