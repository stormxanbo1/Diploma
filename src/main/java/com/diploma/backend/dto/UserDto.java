// src/main/java/com/diploma/backend/dto/UserDto.java
package com.diploma.backend.dto;

import com.diploma.backend.entity.Role;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private Set<Role> roles;
}
