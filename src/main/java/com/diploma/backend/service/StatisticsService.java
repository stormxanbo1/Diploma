package com.diploma.backend.service;

import com.diploma.backend.dto.StatisticsDto;
import com.diploma.backend.entity.Statistics;
import com.diploma.backend.entity.StatisticsType;
import com.diploma.backend.exception.ResourceNotFoundException;
import com.diploma.backend.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    @Transactional
    public StatisticsDto createStatistics(StatisticsType type, String targetId, 
                                        LocalDateTime startDate, LocalDateTime endDate, String data) {
        Statistics statistics = new Statistics();
        statistics.setType(type);
        statistics.setTargetId(targetId);
        statistics.setStartDate(startDate);
        statistics.setEndDate(endDate);
        statistics.setData(data);

        statistics = statisticsRepository.save(statistics);
        return convertToDto(statistics);
    }

    public Page<StatisticsDto> getStatisticsByTypeAndTarget(StatisticsType type, String targetId, Pageable pageable) {
        return statisticsRepository.findByTypeAndTargetId(type, targetId, pageable)
                .map(this::convertToDto);
    }

    public List<StatisticsDto> getStatisticsByTypeAndTargetAndDateRange(
            StatisticsType type, String targetId, LocalDateTime startDate, LocalDateTime endDate) {
        return statisticsRepository.findByTypeAndTargetIdAndStartDateBetween(type, targetId, startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<StatisticsDto> getStatisticsByType(StatisticsType type, Pageable pageable) {
        return statisticsRepository.findByType(type, pageable)
                .map(this::convertToDto);
    }

    public List<StatisticsDto> getStatisticsByTypeAndDateRange(
            StatisticsType type, LocalDateTime startDate, LocalDateTime endDate) {
        return statisticsRepository.findByTypeAndStartDateBetween(type, startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * ?") // Каждый день в полночь
    @Transactional
    public void generateDailyStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        // Генерация статистики по студентам
        generateStudentStatistics(startOfDay, endOfDay);
        
        // Генерация статистики по группам
        generateGroupStatistics(startOfDay, endOfDay);
        
        // Генерация статистики по преподавателям
        generateTeacherStatistics(startOfDay, endOfDay);
    }

    private void generateStudentStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: Реализовать генерацию статистики по студентам
    }

    private void generateGroupStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: Реализовать генерацию статистики по группам
    }

    private void generateTeacherStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: Реализовать генерацию статистики по преподавателям
    }

    private StatisticsDto convertToDto(Statistics statistics) {
        return new StatisticsDto(
                statistics.getId(),
                statistics.getType(),
                statistics.getTargetId(),
                statistics.getStartDate(),
                statistics.getEndDate(),
                statistics.getData(),
                statistics.getCreatedAt(),
                statistics.getUpdatedAt()
        );
    }
} 
