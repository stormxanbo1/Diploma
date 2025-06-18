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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/attachments")
    public ResponseEntity<AttachmentDto> upload(
            @RequestParam("file") MultipartFile file) throws Exception {
        Attachment att = attachmentService.store(file);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(att.getId())
                .toUri();
        return ResponseEntity.created(location).body(toDto(att));
    }

    @GetMapping("/attachments")
    public List<AttachmentDto> listAll() {
        return attachmentService.getAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/tasks/{taskId}/attachments")
    public List<AttachmentDto> listByTask(@PathVariable UUID taskId) {
        return attachmentService.listByTask(taskId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/projects/{projectId}/attachments")
    public List<AttachmentDto> listByProject(@PathVariable UUID projectId) {
        return attachmentService.listByProject(projectId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/attachments/{id}")
    public ResponseEntity<AttachmentDto> getMetadata(@PathVariable UUID id) {
        Attachment att = attachmentService.getMetadata(id);
        return ResponseEntity.ok(toDto(att));
    }

    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID id) throws Exception {
        Attachment att = attachmentService.getMetadata(id);
        InputStream is = attachmentService.getObjectStream(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(att.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + att.getFileName() + "\"")
                .body(new InputStreamResource(is));
    }

    @DeleteMapping("/attachments/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) throws Exception {
        attachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/tasks/{taskId}/attachments/{id}")
    public ResponseEntity<Void> deleteFromTask(@PathVariable UUID taskId, @PathVariable UUID id) throws Exception {
        attachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/projects/{projectId}/attachments/{id}")
    public ResponseEntity<Void> deleteFromProject(@PathVariable UUID projectId, @PathVariable UUID id) throws Exception {
        attachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private AttachmentDto toDto(Attachment att) {
        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/attachments/")
                .path(att.getId().toString())
                .path("/download")
                .toUriString();
        return AttachmentDto.builder()
                .id(att.getId())
                .fileName(att.getFileName())
                .contentType(att.getContentType())
                .size(att.getSize())
                .url(downloadUrl)
                .uploadedAt(att.getUploadedAt())
                .build();
    }
}
