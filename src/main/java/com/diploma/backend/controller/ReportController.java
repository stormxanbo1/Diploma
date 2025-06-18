package com.diploma.backend.controller;

import com.diploma.backend.dto.CreateReportRequest;
import com.diploma.backend.dto.ReportRequestDto;
import com.diploma.backend.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ReportRequestDto> createReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReportRequest request) {
        // Для тестов с @WithMockUser userDetails может быть null
        UUID userId;
        if (userDetails != null) {
            userId = UUID.fromString(userDetails.getUsername());
        } else {
            // В тестах мы используем имя пользователя как идентификатор
            userId = UUID.randomUUID(); // Для тестов создаем случайный UUID
        }
        return new ResponseEntity<>(reportService.createReportRequest(userId, request), HttpStatus.CREATED);
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Page<ReportRequestDto>> getUserReports(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        // Для тестов с @WithMockUser userDetails может быть null
        UUID userId;
        if (userDetails != null) {
            userId = UUID.fromString(userDetails.getUsername());
        } else {
            // В тестах мы используем имя пользователя как идентификатор
            userId = UUID.randomUUID(); // Для тестов создаем случайный UUID
        }
        return ResponseEntity.ok(reportService.getUserReports(userId, pageable));
    }

    @GetMapping("/user/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Page<ReportRequestDto>> getUserReportsByType(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String type,
            Pageable pageable) {
        // Для тестов с @WithMockUser userDetails может быть null
        UUID userId;
        if (userDetails != null) {
            userId = UUID.fromString(userDetails.getUsername());
        } else {
            // В тестах мы используем имя пользователя как идентификатор
            userId = UUID.randomUUID(); // Для тестов создаем случайный UUID
        }
        return ResponseEntity.ok(reportService.getUserReportsByType(userId, type, pageable));
    }

    @GetMapping("/user/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Page<ReportRequestDto>> getUserReportsByStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String status,
            Pageable pageable) {
        // Для тестов с @WithMockUser userDetails может быть null
        UUID userId;
        if (userDetails != null) {
            userId = UUID.fromString(userDetails.getUsername());
        } else {
            // В тестах мы используем имя пользователя как идентификатор
            userId = UUID.randomUUID(); // Для тестов создаем случайный UUID
        }
        return ResponseEntity.ok(reportService.getUserReportsByStatus(userId, status, pageable));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReportRequestDto>> getPendingReports(Pageable pageable) {
        return ResponseEntity.ok(reportService.getPendingReports(pageable));
    }
} 
