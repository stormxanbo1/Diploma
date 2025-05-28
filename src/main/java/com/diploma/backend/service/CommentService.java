package com.diploma.backend.service;

import com.diploma.backend.dto.CommentDto;
import com.diploma.backend.dto.CreateCommentRequest;
import com.diploma.backend.entity.*;
import com.diploma.backend.exception.NotFoundException;
import com.diploma.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
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

        // Логируем авторизацию
        if (author == null) {
            log.error("User with email {} not found", authorEmail);
        }

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

    private CommentDto toDto(Comment c) {
        // Маппируем Comment в CommentDto
        CommentDto dto = mapper.map(c, CommentDto.class);
        // Явное объединение firstName и lastName в одно поле authorName
        if (c.getAuthor() != null) {
            dto.setAuthorName(c.getAuthor().getFirstName() + " " + c.getAuthor().getLastName());
            dto.setAuthorId(c.getAuthor().getId());
        } else {
            // Если автор отсутствует, устанавливаем значения по умолчанию
            dto.setAuthorName("");
            dto.setAuthorId(null);
        }
        return dto;
    }
}