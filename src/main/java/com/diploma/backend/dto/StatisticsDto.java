package com.diploma.backend.dto;

import com.diploma.backend.entity.StatisticsType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDto {
    private UUID id;
    private StatisticsType type;
    private String targetId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String data;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 
