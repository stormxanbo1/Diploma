package com.diploma.backend;

import com.diploma.backend.config.TestConfig;
import com.diploma.backend.config.TestSecurityConfig;
import com.diploma.backend.dto.CreateTaskRequest;
import com.diploma.backend.entity.Priority;
import com.diploma.backend.entity.Task;
import com.diploma.backend.entity.User;
import com.diploma.backend.entity.Role;
import com.diploma.backend.entity.TaskStatus;
import com.diploma.backend.repository.TaskRepository;
import com.diploma.backend.repository.UserRepository;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Проверяем ролевой доступ к задачам
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class TaskControllerSecurityTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;
    @Autowired TaskRepository taskRepository;
    @Autowired UserRepository userRepository;

    private User teacherUser;

    @BeforeEach
    void setUp() {
        // Создаем пользователя с ролью TEACHER для тестов
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("teacher-" + SecurityHelpers.rnd() + "@example.com");
        user.setFirstName("Teacher");
        user.setLastName("Test");
        user.setPassword("password");
        
        Set<Role> roles = new HashSet<>();
        roles.add(Role.TEACHER);
        user.setRoles(roles);
        
        teacherUser = userRepository.save(user);
    }

    @Test
    @DisplayName("STUDENT не может создавать задачу → 403")
    @WithMockUser(roles = "STUDENT")
    void studentCreateForbidden() throws Exception {
        CreateTaskRequest req = new CreateTaskRequest();
        req.setTitle("T");
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TEACHER создаёт задачу")
    @WithMockUser(roles = "TEACHER", username = "teacher@example.com")
    void teacherCreatesTask() throws Exception {
        // teacher создаёт
        CreateTaskRequest req = new CreateTaskRequest();
        req.setTitle("Teacher Task");
        req.setPriority(Priority.HIGH);
        req.setStatus(TaskStatus.TODO);

        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
                
        String location = result.getResponse().getHeader("Location");
        
        // teacher может читать свою задачу
        mockMvc.perform(get(location))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("STUDENT не может читать задачу TEACHER")
    @WithMockUser(roles = "STUDENT")
    void studentCannotReadTeacherTask() throws Exception {
        // Создаем задачу напрямую через репозиторий
        Task task = new Task();
        task.setTitle("Teacher Task");
        task.setPriority(Priority.HIGH);
        task.setCreator(teacherUser); // Устанавливаем создателя
        task.setStatus(TaskStatus.TODO);
        Task savedTask = taskRepository.save(task);
        
        // student пытается GET
        mockMvc.perform(get("/api/tasks/" + savedTask.getId()))
                .andExpect(status().isForbidden());
    }
}
