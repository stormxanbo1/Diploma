package com.diploma.backend.entity;

public enum ReportStatus {
    PENDING,    // Ожидает обработки
    PROCESSING, // В процессе генерации
    COMPLETED,  // Успешно завершен
    FAILED      // Ошибка при генерации
} 