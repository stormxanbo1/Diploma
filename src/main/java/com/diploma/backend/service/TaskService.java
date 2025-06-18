package com.diploma.backend.service;

import com.diploma.backend.dto.CreateTaskRequest;
import com.diploma.backend.dto.TaskDto;
import com.diploma.backend.entity.*;
import com.diploma.backend.exception.NotFoundException;
import com.diploma.backend.repository.TaskRepository;
import com.diploma.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;


    public TaskDto create(CreateTaskRequest req, String userEmail) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Task task = Task.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .status(req.getStatus() != null ? req.getStatus() : TaskStatus.TODO)
                .priority(req.getPriority() != null ? req.getPriority() : Priority.MEDIUM)
                .weight(req.getWeight())
                .deadline(req.getDeadline())
                .project(req.getProjectId() != null
                        ? Project.builder().id(req.getProjectId()).build()
                        : null)
                .creator(creator)                     // ← вместо owner(...)
                .build();

        // Инициализируем множество исполнителей
        Set<User> assignees = req.getAssigneeIds() != null
                ? new HashSet<>(userRepository.findAllById(req.getAssigneeIds()))
                : new HashSet<>();
        task.setAssignees(assignees);

        Task saved = taskRepository.save(task);
        return toDto(saved);
    }

    public List<TaskDto> listMyTasks(String userEmail) {
        User me = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return taskRepository.findByAssignees_Id(me.getId()).stream()
                .map(this::toDto)                    // ← вместо mapper.map(..., TaskDto.class)
                .collect(Collectors.toList());
    }

    public TaskDto getById(UUID id, String userEmail) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        checkAccess(task, userEmail);
        return toDto(task);
    }

    public TaskDto update(UUID id, CreateTaskRequest req, String userEmail) {
        checkAccess(taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found")), userEmail);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setStatus(req.getStatus());
        task.setPriority(req.getPriority());
        task.setWeight(req.getWeight());
        task.setDeadline(req.getDeadline());

        Set<User> assignees = req.getAssigneeIds() != null
                ? new HashSet<>(userRepository.findAllById(req.getAssigneeIds()))
                : new HashSet<>();
        task.setAssignees(assignees);

        Task updated = taskRepository.save(task);
        return toDto(updated);
    }

    public void delete(UUID id, String userEmail) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        checkAccess(task, userEmail);
        taskRepository.delete(task);
    }

    public TaskDto addAssignees(UUID id, Set<UUID> assigneeIds, String userEmail) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        checkAccess(task, userEmail);

        Set<User> toAdd = new

                HashSet<>(userRepository.findAllById(assigneeIds));
        if (toAdd.isEmpty()) {
            throw new NotFoundException("No users found for given IDs");
        }
        task.getAssignees().addAll(toAdd);     // ← вместо task.addAssignee(...)

        Task updated = taskRepository.save(task);
        return toDto(updated);
    }

    private void checkAccess(Task task, String userEmail) {
        boolean isCreator = task.getCreator().getEmail().equals(userEmail);  // ← вместо getOwner()
        boolean isStaffOrAdmin = userRepository.findByEmail(userEmail)
                .map(u -> u.getRoles().stream()
                        .anyMatch(r -> r.name().equals("ADMIN") || r.name().equals("STAFF")))
                .orElse(false);
        if (!isCreator && !isStaffOrAdmin) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private TaskDto toDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setWeight(task.getWeight());
        dto.setDeadline(task.getDeadline());
        dto.setCreatorId(task.getCreator() != null ? task.getCreator().getId() : null);
        dto.setAssigneeIds(task.getAssignees() != null
                ? task.getAssignees().stream().map(User::getId).collect(Collectors.toSet())
                : new HashSet<>());
        // Добавь остальные нужные поля из Task в TaskDto здесь
        return dto;
    }

}
