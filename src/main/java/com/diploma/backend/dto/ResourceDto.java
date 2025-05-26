// src/main/java/com/diploma/backend/dto/ResourceDto.java
package com.diploma.backend.dto;

import com.diploma.backend.entity.Resource;
import com.diploma.backend.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class ResourceDto {
    private UUID id;
    private String name;
    private String type;
    private URI url;
    private Set<Role> allowedRoles;
    private Set<UUID> allowedGroupIds;

    /**
     * Маппинг сущности в DTO с гарантией, что наборы не null.
     */
    public static ResourceDto fromEntity(Resource r) {
        return ResourceDto.builder()
                .id(r.getId())
                .name(r.getName())
                .type(r.getType())
                .url(URI.create(r.getUrl()))
                .allowedRoles(
                        r.getAllowedRoles() != null
                                ? r.getAllowedRoles()
                                : Collections.emptySet()
                )
                .allowedGroupIds(
                        r.getAllowedGroupIds() != null
                                ? r.getAllowedGroupIds()
                                : Collections.emptySet()
                )
                .build();
    }
}
