
package com.diploma.backend.service;

import com.diploma.backend.dto.CreateProjectRequest;
import com.diploma.backend.dto.ProjectDto;
import com.diploma.backend.entity.Project;
import com.diploma.backend.entity.User;
import com.diploma.backend.exception.NotFoundException;
import com.diploma.backend.repository.ProjectRepository;
import com.diploma.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;

    public ProjectDto create(CreateProjectRequest req, String creatorEmail) {
        User creator = userRepo.findByEmail(creatorEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Set<User> participants = new HashSet<>();
        if (req.getParticipantIds() != null && !req.getParticipantIds().isEmpty()) {
            participants = new HashSet<>(userRepo.findAllById(req.getParticipantIds()));
        }

        Project project = Project.builder()
                .name(req.getName())
                .description(req.getDescription())
                .category(req.getCategory())
                .deadline(req.getDeadline())
                .creator(creator)
                .participants(participants)
                .build();

        Project saved = projectRepo.save(project);
        return toDto(saved);
    }

    public List<ProjectDto> listMy(String userEmail) {
        // STAFF/ADMIN видят все, остальные – только свои и где участвуют
        boolean isStaffOrAdmin = userRepo.findByEmail(userEmail)
                .map(u -> u.getRoles().stream()
                        .anyMatch(r -> r.name().equals("STAFF") || r.name().equals("ADMIN")))
                .orElse(false);

        List<Project> projects = isStaffOrAdmin
                ? projectRepo.findAll()
                : projectRepo.findByCreator_EmailOrParticipants_Email(userEmail, userEmail);

        return projects.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ProjectDto> listFiltered(UUID ownerId, String category) {
        List<Project> projects;

        if (ownerId != null && category != null) {
            // Фильтруем по обоим параметрам
            projects = projectRepo.findByCreator_IdOrParticipants_Id(ownerId, ownerId)
                    .stream()
                    .filter(p -> category.equals(p.getCategory()))
                    .collect(Collectors.toList());
        } else if (ownerId != null) {
            // Только по владельцу/участнику
            projects = projectRepo.findByCreator_IdOrParticipants_Id(ownerId, ownerId);
        } else {
            // Только по категории
            projects = projectRepo.findByCategory(category);
        }

        return projects.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProjectDto getById(UUID id, String userEmail) {
        Project project = projectRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        checkAccess(project, userEmail);
        return toDto(project);
    }

    public ProjectDto update(UUID id, CreateProjectRequest req, String userEmail) {
        Project project = projectRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        checkAccess(project, userEmail);

        project.setName(req.getName());
        project.setDescription(req.getDescription());
        project.setCategory(req.getCategory());
        project.setDeadline(req.getDeadline());

        // Обновляем участников если указаны
        if (req.getParticipantIds() != null) {
            Set<User> participants = new HashSet<>(userRepo.findAllById(req.getParticipantIds()));

            project.setParticipants(participants);
        }

        Project updated = projectRepo.save(project);
        return toDto(updated);
    }

    public void delete(UUID id, String userEmail) {
        Project project = projectRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        checkAccess(project, userEmail);
        projectRepo.delete(project);
    }

    private void checkAccess(Project project, String userEmail) {
        boolean isCreator = project.getCreator().getEmail().equals(userEmail);
        boolean isParticipant = project.getParticipants().stream()
                .anyMatch(p -> p.getEmail().equals(userEmail));
        boolean isStaffOrAdmin = userRepo.findByEmail(userEmail)
                .map(u -> u.getRoles().stream()
                        .anyMatch(r -> r.name().equals("STAFF") || r.name().equals("ADMIN")))
                .orElse(false);

        if (!isCreator && !isParticipant && !isStaffOrAdmin) {
            throw new SecurityException("Доступ запрещён");
        }
    }

    /**
     * Ручной маппинг сущности Project в DTO.
     */
    private ProjectDto toDto(Project p) {
        return ProjectDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .category(p.getCategory())
                .deadline(p.getDeadline())
                .creatorId(p.getCreator().getId())
                .participantIds(p.getParticipants().stream()
                        .map(User::getId)
                        .collect(Collectors.toSet()))
                .createdAt(p.getCreatedAt())
                .build();
    }
}