package com.diploma.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private String bucket;

    @Column(nullable = false)
    private String objectName;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    /** Привязка к задаче */
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    /** Привязка к проекту */
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}
