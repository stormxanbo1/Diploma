// src/main/java/com/diploma/backend/repository/AttachmentRepository.java
package com.diploma.backend.repository;

import com.diploma.backend.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByTask_Id(UUID taskId);

    List<Attachment> findByProject_Id(UUID projectId);
}
