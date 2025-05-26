package com.diploma.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {
    @NotBlank(message = "Поле refreshToken не должно быть пустым")
    private String refreshToken;
}
