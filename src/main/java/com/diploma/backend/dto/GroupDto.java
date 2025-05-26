// src/main/java/com/diploma/backend/dto/GroupDto.java
package com.diploma.backend.dto;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class GroupDto {
    private UUID id;
    private String name;
    private UUID creatorId;
    private Set<UUID> memberIds;
}
