package com.diploma.backend.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class CreateProjectRequest {
    @NotBlank
    private String name;
    private String description;
    private String category;
    private LocalDateTime deadline;
    private Set<UUID> participantIds;
}
