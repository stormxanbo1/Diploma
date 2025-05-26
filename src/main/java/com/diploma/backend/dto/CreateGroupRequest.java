package com.diploma.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class CreateGroupRequest {
    @NotBlank
    private String name;
    private Set<UUID> memberIds;
}
