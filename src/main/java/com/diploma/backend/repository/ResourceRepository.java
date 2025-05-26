// src/main/java/com/diploma/backend/repository/ResourceRepository.java
package com.diploma.backend.repository;

import com.diploma.backend.entity.Resource;
import com.diploma.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    @Query("""
        SELECT DISTINCT r
        FROM Resource r
        LEFT JOIN r.allowedGroupIds gid
        WHERE
          (r.allowedRoles IS EMPTY OR :role MEMBER OF r.allowedRoles)
          OR (gid IN :groupIds)
        """)
    List<Resource> findAccessibleResourcesByRoleOrGroups(
            @Param("role")      Role       role,
            @Param("groupIds") List<UUID> groupIds
    );
}
