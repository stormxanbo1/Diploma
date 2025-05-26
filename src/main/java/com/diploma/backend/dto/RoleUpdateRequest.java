// src/main/java/com/diploma/backend/dto/RoleUpdateRequest.java
package com.diploma.backend.dto;

import com.diploma.backend.entity.Role;
import lombok.Data;

import java.util.Set;

@Data
public class RoleUpdateRequest {
    private Set<Role> roles;
}
