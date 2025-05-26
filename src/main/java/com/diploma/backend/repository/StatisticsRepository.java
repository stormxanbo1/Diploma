package com.diploma.backend.repository;

import com.diploma.backend.entity.Statistics;
import com.diploma.backend.entity.StatisticsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, UUID> {
    
    Page<Statistics> findByTypeAndTargetId(StatisticsType type, String targetId, Pageable pageable);
    
    List<Statistics> findByTypeAndTargetIdAndStartDateBetween(
            StatisticsType type,
            String targetId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    
    Page<Statistics> findByType(StatisticsType type, Pageable pageable);
    
    List<Statistics> findByTypeAndStartDateBetween(
            StatisticsType type,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
} 