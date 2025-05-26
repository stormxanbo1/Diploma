// src/main/java/com/diploma/backend/repository/GroupRepository.java
package com.diploma.backend.repository;

import com.diploma.backend.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    /**
     * Находит все группы, где пользователь с данным email является создателем.
     */
    List<Group> findByCreator_Email(String creatorEmail);

    /**
     * Находит все группы, в которых пользователь с данным userId состоит в members.
     */
    List<Group> findByMembers_Id(UUID userId);
}
