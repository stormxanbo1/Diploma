package com.diploma.backend.dto;

import com.diploma.backend.entity.LessonType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEntryDto {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private UUID teacherId;
    private String teacherFullName;
    private String subject;
    private String description;
    private String location;
    private LessonType lessonType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;           // ← renamed from isActive to active
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
