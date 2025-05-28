package com.diploma.backend.controller;

import com.diploma.backend.dto.CommentDto;
import com.diploma.backend.dto.CreateCommentRequest;
import com.diploma.backend.service.CommentService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<CommentDto> addToTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest req,
            Authentication auth) {
        CommentDto dto = commentService.addToTask(taskId, req, auth.getName());
        return ResponseEntity
                .created(URI.create("/api/tasks/" + taskId + "/comments/" + dto.getId()))
                .body(dto);
    }

    @GetMapping("/tasks/{taskId}/comments")
    public List<CommentDto> listTask(@PathVariable UUID taskId) {
        return commentService.listTask(taskId);
    }

    @PostMapping("/projects/{projectId}/comments")
    public ResponseEntity<CommentDto> addToProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateCommentRequest req,
            Authentication auth) {
        CommentDto dto = commentService.addToProject(projectId, req, auth.getName());
        return ResponseEntity
                .created(URI.create("/api/projects/" + projectId + "/comments/" + dto.getId()))
                .body(dto);
    }

    @GetMapping("/projects/{projectId}/comments")
    public List<CommentDto> listProject(@PathVariable UUID projectId) {
        return commentService.listProject(projectId);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication auth) {
        commentService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
