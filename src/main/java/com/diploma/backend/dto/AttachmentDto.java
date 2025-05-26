// src/main/java/com/diploma/backend/dto/AttachmentDto.java
package com.diploma.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AttachmentDto {
    private UUID id;
    private String fileName;
    private String contentType;
    private long size;
    private String url;
    private LocalDateTime uploadedAt;
}
