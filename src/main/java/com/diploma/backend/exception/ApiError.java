package com.diploma.backend.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Единый формат ответа об ошибке API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    /** Код ошибки, например NOT_FOUND, VALIDATION_ERROR */
    private String code;
    /** Человекочитаемое сообщение */
    private String message;
    /** Момент времени, когда произошла ошибка */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}