// src/main/java/com/diploma/backend/repository/ProjectRepository.java
package com.diploma.backend.repository;

import com.diploma.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    /** Метод для списка «моих» проектов */
    List<Project> findByCreator_Email(String email);
    
    /** Поиск по создателю или участнику */
    List<Project> findByCreator_IdOrParticipants_Id(UUID creatorId, UUID participantId);
    
    /** Поиск по категории */
    List<Project> findByCategory(String category);
    
    /** Поиск по email создателя или участника */
    List<Project> findByCreator_EmailOrParticipants_Email(String creatorEmail, String participantEmail);
}
