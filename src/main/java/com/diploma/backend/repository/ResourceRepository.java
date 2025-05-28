package com.diploma.backend.repository;

import com.diploma.backend.entity.Resource;
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
        WHERE gid IN :groupIds OR SIZE(r.allowedGroupIds) = 0
        """)
    List<Resource> findAccessibleResourcesByGroups(@Param("groupIds") List<UUID> groupIds);

    // Метод для получения ресурсов без ограничений по группам
    @Query("SELECT r FROM Resource r WHERE SIZE(r.allowedGroupIds) = 0")
    List<Resource> findResourcesWithoutGroupRestrictions();

    // Если нужен метод для админов (получить все ресурсы)
    @Query("SELECT r FROM Resource r")
    List<Resource> findAll();
}