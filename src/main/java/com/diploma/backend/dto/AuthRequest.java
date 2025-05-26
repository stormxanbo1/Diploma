package com.diploma.backend.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class AuthRequest {
    @Email @NotBlank
    private String email;
    @NotBlank
    private String password;
}
