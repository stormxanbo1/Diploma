// src/main/java/com/diploma/backend/dto/CommentDto.java
package com.diploma.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CommentDto {
    private UUID id;
    private String text;
    private UUID authorId;
    private String authorName;
    private LocalDateTime createdAt;
}
