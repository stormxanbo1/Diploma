package com.diploma.backend.dto;

import com.diploma.backend.entity.Priority;
import com.diploma.backend.entity.TaskStatus;
import lombok.Data;
import java.time.*;
import java.util.*;

@Data
public class TaskDto {
    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private Integer weight;
    private LocalDateTime deadline;
    private UUID projectId;
    private Set<UUID> assigneeIds;
    private UUID creatorId;
}
