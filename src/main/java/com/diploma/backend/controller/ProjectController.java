// src/main/java/com/diploma/backend/controller/ProjectController.java
package com.diploma.backend.controller;

import com.diploma.backend.dto.ProjectDto;
import com.diploma.backend.dto.CreateProjectRequest;
import com.diploma.backend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Validated
public class ProjectController {

    private final ProjectService projectService;

    /** Создание нового проекта с указанием автора */
    @PostMapping
    public ResponseEntity<ProjectDto> create(
            @Valid @RequestBody CreateProjectRequest req,
            Authentication auth) {
        ProjectDto dto = projectService.create(req, auth.getName());
        URI location = URI.create("/api/projects/" + dto.getId());
        return ResponseEntity.created(location).body(dto);
    }

    /** Список проектов с фильтрацией */
    @GetMapping
    public List<ProjectDto> list(
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) String category,
            Authentication auth) {
        if (ownerId != null || category != null) {
            return projectService.listFiltered(ownerId, category);
        }
        return projectService.listMy(auth.getName());
    }

    /** Получить проект по ID */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getById(
            @PathVariable UUID id,
            Authentication auth) {
        ProjectDto dto = projectService.getById(id, auth.getName());
        return ResponseEntity.ok(dto);
    }

    /** Обновление проекта */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProjectRequest req,
            Authentication auth) {
        ProjectDto dto = projectService.update(id, req, auth.getName());
        return ResponseEntity.ok(dto);
    }

    /** Удаление проекта */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication auth) {
        projectService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
