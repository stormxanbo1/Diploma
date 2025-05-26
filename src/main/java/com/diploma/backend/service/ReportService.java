package com.diploma.backend.service;

import com.diploma.backend.dto.CreateReportRequest;
import com.diploma.backend.dto.ReportRequestDto;
import com.diploma.backend.entity.ReportRequest;
import com.diploma.backend.entity.ReportStatus;
import com.diploma.backend.entity.ReportType;
import com.diploma.backend.entity.User;
import com.diploma.backend.exception.ResourceNotFoundException;
import com.diploma.backend.repository.ReportRequestRepository;
import com.diploma.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRequestRepository reportRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReportRequestDto createReportRequest(UUID requesterId, CreateReportRequest request) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requesterId));

        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setRequester(requester);
        reportRequest.setType(request.getType());
        reportRequest.setStartDate(request.getStartDate());
        reportRequest.setEndDate(request.getEndDate());
        reportRequest.setParameters(request.getParameters());

        reportRequest = reportRequestRepository.save(reportRequest);
        
        // Асинхронно запускаем генерацию отчета
        generateReport(reportRequest.getId());
        
        return convertToDto(reportRequest);
    }

    public Page<ReportRequestDto> getUserReports(UUID userId, Pageable pageable) {
        return reportRequestRepository.findByRequesterId(userId, pageable)
                .map(this::convertToDto);
    }

    public Page<ReportRequestDto> getUserReportsByType(UUID userId, String type, Pageable pageable) {
        return reportRequestRepository.findByRequesterIdAndType(userId, ReportType.valueOf(type), pageable)
                .map(this::convertToDto);
    }

    public Page<ReportRequestDto> getUserReportsByStatus(UUID userId, String status, Pageable pageable) {
        return reportRequestRepository.findByRequesterIdAndStatus(userId, ReportStatus.valueOf(status), pageable)
                .map(this::convertToDto);
    }

    public Page<ReportRequestDto> getPendingReports(Pageable pageable) {
        return reportRequestRepository.findByStatus(ReportStatus.PENDING, pageable)
                .map(this::convertToDto);
    }

    @Async
    protected void generateReport(UUID reportId) {
        ReportRequest reportRequest = reportRequestRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report request not found with id: " + reportId));

        try {
            reportRequest.setStatus(ReportStatus.PROCESSING);
            reportRequestRepository.save(reportRequest);

            // Здесь будет логика генерации отчета в зависимости от типа
            String result = generateReportByType(reportRequest);

            reportRequest.setStatus(ReportStatus.COMPLETED);
            reportRequest.setResult(result);
        } catch (Exception e) {
            reportRequest.setStatus(ReportStatus.FAILED);
            reportRequest.setErrorMessage(e.getMessage());
        }

        reportRequestRepository.save(reportRequest);
    }

    private String generateReportByType(ReportRequest reportRequest) {
        // TODO: Реализовать генерацию отчетов разных типов
        return "Report generation not implemented yet";
    }

    private ReportRequestDto convertToDto(ReportRequest reportRequest) {
        return new ReportRequestDto(
                reportRequest.getId(),
                reportRequest.getRequester().getId(),
                reportRequest.getType(),
                reportRequest.getCreatedAt(),
                reportRequest.getStartDate(),
                reportRequest.getEndDate(),
                reportRequest.getParameters(),
                reportRequest.getStatus(),
                reportRequest.getResult(),
                reportRequest.getErrorMessage()
        );
    }
} 