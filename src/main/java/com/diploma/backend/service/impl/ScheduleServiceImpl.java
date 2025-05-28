package com.diploma.backend.service.impl;

import com.diploma.backend.dto.CreateScheduleEntryRequest;
import com.diploma.backend.dto.ScheduleEntryDto;
import com.diploma.backend.entity.Group;
import com.diploma.backend.entity.ScheduleEntry;
import com.diploma.backend.entity.User;
import com.diploma.backend.exception.NotFoundException;
import com.diploma.backend.repository.GroupRepository;
import com.diploma.backend.repository.ScheduleEntryRepository;
import com.diploma.backend.repository.UserRepository;
import com.diploma.backend.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleEntryRepository scheduleEntryRepository;
    private final GroupRepository           groupRepository;
    private final UserRepository            userRepository;

    @Override
    @Transactional
    public ScheduleEntryDto createScheduleEntry(CreateScheduleEntryRequest req) {
        Group group = groupRepository.findById(req.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found: " + req.getGroupId()));
        User teacher = userRepository.findById(req.getTeacherId())
                .orElseThrow(() -> new NotFoundException("User not found: " + req.getTeacherId()));

        ScheduleEntry entry = ScheduleEntry.builder()
                .group(group)
                .teacher(teacher)
                .subject(req.getSubject())
                .description(req.getDescription())
                .location(req.getLocation())
                .lessonType(req.getLessonType())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .active(true)
                .build();

        ScheduleEntry saved = scheduleEntryRepository.saveAndFlush(entry);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public ScheduleEntryDto updateScheduleEntry(UUID id, CreateScheduleEntryRequest req) {
        ScheduleEntry entry = scheduleEntryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ScheduleEntry not found: " + id));

        Group group = groupRepository.findById(req.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found: " + req.getGroupId()));
        User teacher = userRepository.findById(req.getTeacherId())
                .orElseThrow(() -> new NotFoundException("User not found: " + req.getTeacherId()));

        entry.setGroup(group);
        entry.setTeacher(teacher);
        entry.setSubject(req.getSubject());
        entry.setDescription(req.getDescription());
        entry.setLocation(req.getLocation());
        entry.setLessonType(req.getLessonType());
        entry.setStartTime(req.getStartTime());
        entry.setEndTime(req.getEndTime());

        ScheduleEntry updated = scheduleEntryRepository.save(entry);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void deleteScheduleEntry(UUID id) {
        if (!scheduleEntryRepository.existsById(id)) {
            throw new NotFoundException("ScheduleEntry not found: " + id);
        }
        scheduleEntryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScheduleEntryDto> getAllScheduleEntries(LocalDate date, Pageable pageable) {
        List<ScheduleEntryDto> dtos;

        if (date == null) {
            dtos = scheduleEntryRepository.findAll(pageable).getContent()
                    .stream().map(this::mapToDto).collect(Collectors.toList());
        } else {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end   = start.plusDays(1);
            dtos = scheduleEntryRepository.findAll(pageable).getContent().stream()
                    .filter(e -> !e.getStartTime().isBefore(start) && e.getStartTime().isBefore(end))
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(dtos, pageable, dtos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScheduleEntryDto> getScheduleEntriesByGroup(UUID groupId, LocalDate date, Pageable pageable) {
        LocalDateTime start = date != null ? date.atStartOfDay() : LocalDate.MIN.atStartOfDay();
        LocalDateTime end   = date != null ? date.plusDays(1).atStartOfDay() : LocalDate.MAX.atTime(23,59,59);

        List<ScheduleEntryDto> dtos = scheduleEntryRepository
                .findByGroup_IdAndStartTimeBetweenOrderByStartTime(groupId, start, end)
                .stream().map(this::mapToDto).collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, dtos.size());
    }

    private ScheduleEntryDto mapToDto(ScheduleEntry e) {
        ScheduleEntryDto dto = new ScheduleEntryDto();
        dto.setId(e.getId());
        dto.setGroupId(e.getGroup().getId());
        dto.setGroupName(e.getGroup().getName());
        dto.setTeacherId(e.getTeacher().getId());
        dto.setTeacherFullName(e.getTeacher().getFirstName() + " " + e.getTeacher().getLastName());
        dto.setSubject(e.getSubject());
        dto.setDescription(e.getDescription());
        dto.setLocation(e.getLocation());
        dto.setLessonType(e.getLessonType());
        dto.setStartTime(e.getStartTime());
        dto.setEndTime(e.getEndTime());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
