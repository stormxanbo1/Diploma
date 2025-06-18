package com.diploma.backend.repository;

import com.diploma.backend.entity.Group;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    @EntityGraph(attributePaths = "members")
    List<Group> findByCreator_Email(String creatorEmail);
    @EntityGraph(attributePaths = "members")
    List<Group> findByMembers_Id(UUID userId);
    @Override
    @EntityGraph(attributePaths = "members")
    List<Group> findAll();
}
