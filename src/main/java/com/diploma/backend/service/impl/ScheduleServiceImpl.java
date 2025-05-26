package com.diploma.backend.service.impl;

import com.diploma.backend.dto.CreateScheduleEntryRequest;
import com.diploma.backend.dto.ScheduleEntryDto;
import com.diploma.backend.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    
    @Override
    @Transactional
    public ScheduleEntryDto createScheduleEntry(CreateScheduleEntryRequest request) {
        // Заглушка - будет реализована позже
        ScheduleEntryDto dto = new ScheduleEntryDto();
        dto.setId(UUID.randomUUID());
        dto.setGroupId(request.getGroupId());
        dto.setTeacherId(request.getTeacherId());
        return dto;
    }
    
    @Override
    @Transactional
    public ScheduleEntryDto updateScheduleEntry(UUID id, CreateScheduleEntryRequest request) {
        // Заглушка - будет реализована позже
        ScheduleEntryDto dto = new ScheduleEntryDto();
        dto.setId(id);
        dto.setGroupId(request.getGroupId());
        dto.setTeacherId(request.getTeacherId());
        return dto;
    }
    
    @Override
    @Transactional
    public void deleteScheduleEntry(UUID id) {
        // Заглушка - будет реализована позже
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ScheduleEntryDto> getAllScheduleEntries(LocalDate date, Pageable pageable) {
        // Заглушка - будет реализована позже
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ScheduleEntryDto> getScheduleEntriesByGroup(UUID groupId, LocalDate date, Pageable pageable) {
        // Заглушка - будет реализована позже
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }
} 