package com.diploma.backend.dto;

import com.diploma.backend.entity.ReportStatus;
import com.diploma.backend.entity.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDto {
    private UUID id;
    private UUID requesterId;
    private ReportType type;
    private LocalDateTime createdAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String parameters;
    private ReportStatus status;
    private String result;
    private String errorMessage;
} 
