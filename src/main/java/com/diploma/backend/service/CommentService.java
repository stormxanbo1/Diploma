// src/main/java/com/diploma/backend/service/CommentService.java
package com.diploma.backend.service;

import com.diploma.backend.dto.CommentDto;
import com.diploma.backend.dto.CreateCommentRequest;
import com.diploma.backend.entity.*;
import com.diploma.backend.exception.NotFoundException;
import com.diploma.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final TaskRepository taskRepo;
    private final ProjectRepository projectRepo;
    private final CommentRepository commentRepo;
    private final UserRepository userRepo;
    private final ModelMapper mapper;

    /* ---------- создать ---------- */

    @Transactional
    public CommentDto addToTask(UUID taskId, CreateCommentRequest req, String authorEmail) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        User author = userRepo.findByEmail(authorEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Comment c = Comment.builder()
                .text(req.getText())
                .author(author)
                .task(task)
                .build();
        Comment saved = commentRepo.save(c);
        log.debug("Comment created for task {}: {} by user {}", taskId, saved.getId(), authorEmail);
        return toDto(saved);
    }

    @Transactional
    public CommentDto addToProject(UUID projectId, CreateCommentRequest req, String authorEmail) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        User author = userRepo.findByEmail(authorEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Comment c = Comment.builder()
                .text(req.getText())
                .author(author)
                .project(project)
                .build();
        Comment saved = commentRepo.save(c);
        log.debug("Comment created for project {}: {} by user {}", projectId, saved.getId(), authorEmail);
        return toDto(saved);
    }

    /* ---------- списки ---------- */

    @Transactional(readOnly = true)
    public List<CommentDto> listTask(UUID taskId) {
        return commentRepo.findByTask_IdOrderByCreatedAt(taskId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentDto> listProject(UUID projectId) {
        return commentRepo.findByProject_IdOrderByCreatedAt(projectId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    /* ---------- удалить (автор или STAFF/ADMIN) ---------- */

    @Transactional
    public void delete(UUID commentId, String userEmail) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with ID: " + commentId));

        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));

        boolean isAuthor = comment.getAuthor().getId().equals(user.getId());
        boolean isStaffOrAdmin = user.getRoles().stream()
                .anyMatch(r -> r == Role.STAFF || r == Role.ADMIN);

        if (!isAuthor && !isStaffOrAdmin) {
            log.warn("User {} attempted to delete comment {} without permission", userEmail, commentId);
            throw new AccessDeniedException("Вы не можете удалить этот комментарий");
        }

        commentRepo.delete(comment);
        log.debug("Comment {} deleted by user {}", commentId, userEmail);
    }

    /* ---------- helper ---------- */

    private CommentDto toDto(Comment c) {
        CommentDto dto = mapper.map(c, CommentDto.class);
        dto.setAuthorId(c.getAuthor().getId());
        dto.setAuthorName(c.getAuthor().getFirstName() + " " + c.getAuthor().getLastName());
        return dto;
    }
}
