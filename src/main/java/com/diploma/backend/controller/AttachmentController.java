// src/main/java/com/diploma/backend/controller/AttachmentController.java
package com.diploma.backend.controller;

import com.diploma.backend.dto.AttachmentDto;
import com.diploma.backend.entity.Attachment;
import com.diploma.backend.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<AttachmentDto> upload(@RequestParam("file") MultipartFile file) throws Exception {
        Attachment att = attachmentService.store(file);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(att.getId())
                .toUri();

        String downloadUrl = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}/download")
                .buildAndExpand(att.getId())
                .toUriString();

        AttachmentDto dto = AttachmentDto.builder()
                .id(att.getId())
                .fileName(att.getFileName())
                .contentType(att.getContentType())
                .size(att.getSize())
                .url(downloadUrl)
                .uploadedAt(att.getUploadedAt())
                .build();

        return ResponseEntity.created(location).body(dto);
    }

    /**
     * Метаданные вложения, включая URL для скачивания.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AttachmentDto> getMetadata(@PathVariable UUID id) {
        Attachment att = attachmentService.getMetadata(id);
        String downloadUrl = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/download")
                .buildAndExpand()
                .toUriString();

        AttachmentDto dto = AttachmentDto.builder()
                .id(att.getId())
                .fileName(att.getFileName())
                .contentType(att.getContentType())
                .size(att.getSize())
                .url(downloadUrl)
                .uploadedAt(att.getUploadedAt())
                .build();

        return ResponseEntity.ok(dto);
    }

    /**
     * Принудительное скачивание вложения.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID id) throws Exception {
        Attachment att = attachmentService.getMetadata(id);
        InputStream is = attachmentService.getObjectStream(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(att.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + att.getFileName() + "\"")
                .body(new InputStreamResource(is));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) throws Exception {
        attachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
