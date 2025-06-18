package com.diploma.backend.dto;

import com.diploma.backend.entity.TaskStatus;
import com.diploma.backend.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class CreateTaskRequest {
    @NotBlank
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private Integer weight;
    private LocalDateTime deadline;
    private UUID projectId;
    private Set<UUID> assigneeIds;
}
