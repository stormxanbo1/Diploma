package com.diploma.backend.dto;

import com.diploma.backend.entity.LessonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleEntryRequest {
    @NotNull
    private UUID groupId;
    
    @NotNull
    private UUID teacherId;
    
    @NotBlank
    private String subject;
    
    private String description;
    
    @NotBlank
    private String location;
    
    @NotNull
    private LessonType lessonType;
    
    @NotNull
    private LocalDateTime startTime;
    
    @NotNull
    private LocalDateTime endTime;
} 
