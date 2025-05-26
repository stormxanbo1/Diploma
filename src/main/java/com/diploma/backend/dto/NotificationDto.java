package com.diploma.backend.dto;

import com.diploma.backend.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private UUID id;
    private UUID recipientId;
    private String title;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;
    private NotificationType type;
} 