package com.diploma.backend.controller;

import com.diploma.backend.dto.TaskDto;
import com.diploma.backend.dto.CreateTaskRequest;
import com.diploma.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final TaskService taskService;

    /** Создать новую задачу и вернуть Location */
    @PostMapping
    public ResponseEntity<TaskDto> create(
            @Valid @RequestBody CreateTaskRequest req,
            Authentication auth) {
        if (auth == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        
        TaskDto dto = taskService.create(req, auth.getName());
        return ResponseEntity
                .created(URI.create("/api/tasks/" + dto.getId()))
                .body(dto);
    }

    /** Список моих задач */
    @GetMapping
    public List<TaskDto> listMy(Authentication auth) {
        if (auth == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        
        return taskService.listMyTasks(auth.getName());
    }

    /** Получить задачу по ID */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getById(
            @PathVariable UUID id,
            Principal principal) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        
        TaskDto dto = taskService.getById(id, principal.getName());
        return ResponseEntity.ok(dto);
    }

    /** Обновить задачу */
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTaskRequest req,
            Authentication auth) {
        if (auth == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        
        TaskDto dto = taskService.update(id, req, auth.getName());
        return ResponseEntity.ok(dto);
    }

    /** Удалить задачу */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication auth) {
        if (auth == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        
        taskService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    /** Добавить исполнителей */
    @PostMapping("/{id}/assignees")
    public ResponseEntity<TaskDto> addAssignees(
            @PathVariable UUID id,
            @RequestBody Set<UUID> assigneeIds,
            Authentication auth) {
        if (auth == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        
        TaskDto dto = taskService.addAssignees(id, assigneeIds, auth.getName());
        return ResponseEntity.ok(dto);
    }
}
