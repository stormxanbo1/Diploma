package com.diploma.backend.dto;

import com.diploma.backend.entity.ReportType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {
    
    @NotNull(message = "Report type is required")
    private ReportType type;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @NotNull(message = "Parameters are required")
    @Size(max = 1000, message = "Parameters must not exceed 1000 characters")
    private String parameters;
} 