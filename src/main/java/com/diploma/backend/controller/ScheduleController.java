package com.diploma.backend.controller;

import com.diploma.backend.dto.CreateScheduleEntryRequest;
import com.diploma.backend.dto.ScheduleEntryDto;
import com.diploma.backend.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ScheduleEntryDto> createScheduleEntry(
            @RequestBody CreateScheduleEntryRequest request) {
        return new ResponseEntity<>(scheduleService.createScheduleEntry(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleEntryDto> updateScheduleEntry(
            @PathVariable UUID id,
            @RequestBody CreateScheduleEntryRequest request) {
        return ResponseEntity.ok(scheduleService.updateScheduleEntry(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScheduleEntry(@PathVariable UUID id) {
        scheduleService.deleteScheduleEntry(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ScheduleEntryDto>> getAllScheduleEntries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {
        return ResponseEntity.ok(scheduleService.getAllScheduleEntries(date, pageable));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<Page<ScheduleEntryDto>> getScheduleEntriesByGroup(
            @PathVariable UUID groupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {
        return ResponseEntity.ok(scheduleService.getScheduleEntriesByGroup(groupId, date, pageable));
    }
} 
