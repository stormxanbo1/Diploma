package com.diploma.backend.repository;

import com.diploma.backend.entity.ReportRequest;
import com.diploma.backend.entity.ReportStatus;
import com.diploma.backend.entity.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportRequestRepository extends JpaRepository<ReportRequest, UUID> {
    
    Page<ReportRequest> findByRequesterId(UUID requesterId, Pageable pageable);
    
    Page<ReportRequest> findByRequesterIdAndType(UUID requesterId, ReportType type, Pageable pageable);
    
    Page<ReportRequest> findByRequesterIdAndStatus(UUID requesterId, ReportStatus status, Pageable pageable);
    
    Page<ReportRequest> findByStatus(ReportStatus status, Pageable pageable);
} 
