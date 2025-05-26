// src/main/java/com/diploma/backend/dto/CreateUserRequest.java
package com.diploma.backend.dto;

import com.diploma.backend.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class CreateUserRequest {
    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    /** Какие роли выдать сразу (если не укажут — STUDENT) */
    private Set<Role> roles;
}
