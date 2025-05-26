// src/main/java/com/diploma/backend/dto/UpdateUserRequest.java
package com.diploma.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}
