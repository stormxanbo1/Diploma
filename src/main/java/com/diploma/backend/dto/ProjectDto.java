package com.diploma.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.*;
import java.util.*;

@Data
@Builder
public class ProjectDto {
    private UUID id;
    private String name;
    private String description;
    private String category;
    private LocalDateTime deadline;
    private UUID creatorId;
    private Set<UUID> participantIds;
    private LocalDateTime createdAt;
}
