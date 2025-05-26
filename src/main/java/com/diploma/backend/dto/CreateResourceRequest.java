// src/main/java/com/diploma/backend/dto/CreateResourceRequest.java
package com.diploma.backend.dto;

import com.diploma.backend.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class CreateResourceRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String type;

    /** Ссылка на ресурс */
    @NotBlank
    @Pattern(
            regexp = "https?://.+",
            message = "URL должен начинаться с http:// или https://"
    )
    private String url;

    /** Роли, которым разрешён доступ */
    private Set<Role> allowedRoles;

    /** Идентификаторы групп */
    private Set<UUID> allowedGroupIds;
}
