package com.diploma.backend.controller;

import com.diploma.backend.dto.StatisticsDto;
import com.diploma.backend.entity.StatisticsType;
import com.diploma.backend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/{type}/{targetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Page<StatisticsDto>> getStatisticsByTypeAndTarget(
            @PathVariable StatisticsType type,
            @PathVariable String targetId,
            Pageable pageable) {
        return ResponseEntity.ok(statisticsService.getStatisticsByTypeAndTarget(type, targetId, pageable));
    }

    @GetMapping("/{type}/{targetId}/range")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<StatisticsDto>> getStatisticsByTypeAndTargetAndDateRange(
            @PathVariable StatisticsType type,
            @PathVariable String targetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(statisticsService.getStatisticsByTypeAndTargetAndDateRange(
                type, targetId, startDate, endDate));
    }

    @GetMapping("/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<StatisticsDto>> getStatisticsByType(
            @PathVariable StatisticsType type,
            Pageable pageable) {
        return ResponseEntity.ok(statisticsService.getStatisticsByType(type, pageable));
    }

    @GetMapping("/{type}/range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StatisticsDto>> getStatisticsByTypeAndDateRange(
            @PathVariable StatisticsType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(statisticsService.getStatisticsByTypeAndDateRange(type, startDate, endDate));
    }
} 