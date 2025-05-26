package com.diploma.backend.repository;

import com.diploma.backend.entity.ScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, UUID> {
    
    List<ScheduleEntry> findByGroup_IdAndStartTimeBetweenOrderByStartTime(
            UUID groupId, 
            LocalDateTime start, 
            LocalDateTime end
    );

    List<ScheduleEntry> findByTeacher_IdAndStartTimeBetweenOrderByStartTime(
            UUID teacherId, 
            LocalDateTime start, 
            LocalDateTime end
    );

    @Query("SELECT s FROM ScheduleEntry s WHERE " +
           "s.startTime >= :start AND s.startTime < :end AND " +
           "(s.group.id = :groupId OR s.teacher.id = :teacherId) " +
           "ORDER BY s.startTime")
    List<ScheduleEntry> findByGroupOrTeacherAndDateRange(
            @Param("groupId") UUID groupId,
            @Param("teacherId") UUID teacherId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    boolean existsByTeacher_IdAndStartTimeBetween(
            UUID teacherId,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsByGroup_IdAndStartTimeBetween(
            UUID groupId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM ScheduleEntry s " +
           "WHERE s.teacher.id = :teacherId AND " +
           "((s.startTime <= :start AND s.endTime > :start) OR " +
           "(s.startTime < :end AND s.endTime >= :end) OR " +
           "(s.startTime >= :start AND s.endTime <= :end))")
    boolean hasTeacherOverlappingSchedule(
            @Param("teacherId") UUID teacherId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM ScheduleEntry s " +
           "WHERE s.group.id = :groupId AND " +
           "((s.startTime <= :start AND s.endTime > :start) OR " +
           "(s.startTime < :end AND s.endTime >= :end) OR " +
           "(s.startTime >= :start AND s.endTime <= :end))")
    boolean hasGroupOverlappingSchedule(
            @Param("groupId") UUID groupId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
} 