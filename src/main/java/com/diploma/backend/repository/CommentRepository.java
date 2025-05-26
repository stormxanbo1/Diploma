// src/main/java/com/diploma/backend/repository/CommentRepository.java
package com.diploma.backend.repository;

import com.diploma.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByTask_IdOrderByCreatedAt(UUID taskId);
    List<Comment> findByProject_IdOrderByCreatedAt(UUID projectId);
}
