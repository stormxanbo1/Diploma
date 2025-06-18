package com.diploma.backend.service;

import com.diploma.backend.dto.CreateScheduleEntryRequest;
import com.diploma.backend.dto.ScheduleEntryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface ScheduleService {
    
    ScheduleEntryDto createScheduleEntry(CreateScheduleEntryRequest request);
    
    ScheduleEntryDto updateScheduleEntry(UUID id, CreateScheduleEntryRequest request);
    
    void deleteScheduleEntry(UUID id);
    
    Page<ScheduleEntryDto> getAllScheduleEntries(LocalDate date, Pageable pageable);
    
    Page<ScheduleEntryDto> getScheduleEntriesByGroup(UUID groupId, LocalDate date, Pageable pageable);
} 
