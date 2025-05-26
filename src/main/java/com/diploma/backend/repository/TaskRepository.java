package com.diploma.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.diploma.backend.entity.Task;
import java.util.*;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByProject_Id(UUID projectId);
    List<Task> findByAssignees_Id(UUID userId);
}
