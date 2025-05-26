// src/main/java/com/diploma/backend/service/AttachmentService.java
package com.diploma.backend.service;

import com.diploma.backend.entity.Attachment;
import com.diploma.backend.exception.NotFoundException;
import com.diploma.backend.repository.AttachmentRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final MinioClient minioClient;
    private final AttachmentRepository attachmentRepo;

    @Value("${minio.bucket}")
    private String bucket;

    /**
     * Сохраняет файл в MinIO и метаданные в БД.
     */
    public Attachment store(MultipartFile file) throws Exception {
        String objectName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        // Загружаем в MinIO
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        // Сохраняем метаданные
        Attachment att = Attachment.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .bucket(bucket)
                .objectName(objectName)
                .uploadedAt(LocalDateTime.now())
                .build();
        return attachmentRepo.save(att);
    }

    /**
     * Возвращает InputStream файла из MinIO.
     */
    public InputStream getObjectStream(UUID id) throws Exception {
        Attachment att = attachmentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(att.getBucket())
                        .object(att.getObjectName())
                        .build()
        );
    }

    /**
     * Возвращает метаданные вложения.
     */
    public Attachment getMetadata(UUID id) {
        return attachmentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));
    }

    /**
     * Удаляет объект из MinIO и запись из БД.
     */
    public void delete(UUID id) throws Exception {
        Attachment att = getMetadata(id);
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(att.getBucket())
                        .object(att.getObjectName())
                        .build()
        );
        attachmentRepo.delete(att);
    }
}
