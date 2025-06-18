package com.diploma.backend.service;

import com.diploma.backend.entity.Attachment;
import com.diploma.backend.entity.Task;
import com.diploma.backend.entity.Project;
import com.diploma.backend.exception.NotFoundException;
import com.diploma.backend.repository.AttachmentRepository;
import com.diploma.backend.repository.TaskRepository;
import com.diploma.backend.repository.ProjectRepository;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final MinioClient minioClient;
    private final AttachmentRepository attachmentRepo;
    private final TaskRepository taskRepo;
    private final ProjectRepository projectRepo;

    @Value("${minio.bucket}")
    private String bucket;

    public Attachment store(MultipartFile file) throws Exception {
        String objectName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String contentType = file.getContentType() != null
                ? file.getContentType()
                : "application/octet-stream";
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(contentType)
                        .build()
        );
        Attachment att = Attachment.builder()
                .fileName(file.getOriginalFilename())
                .contentType(contentType)
                .size(file.getSize())
                .bucket(bucket)
                .objectName(objectName)
                .uploadedAt(LocalDateTime.now())
                .build();
        return attachmentRepo.save(att);
    }

    public Attachment storeToTask(UUID taskId, MultipartFile file) throws Exception {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        Attachment att = store(file);
        att.setTask(task);
        return attachmentRepo.save(att);
    }

    public Attachment storeToProject(UUID projectId, MultipartFile file) throws Exception {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        Attachment att = store(file);
        att.setProject(project);
        return attachmentRepo.save(att);
    }

    public List<Attachment> getAll() {
        return attachmentRepo.findAll();
    }

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

    public Attachment getMetadata(UUID id) {
        return attachmentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));
    }

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

    public List<Attachment> listByTask(UUID taskId) {
        return attachmentRepo.findByTask_Id(taskId);
    }

    public List<Attachment> listByProject(UUID projectId) {
        return attachmentRepo.findByProject_Id(projectId);
    }
}
