package com.diploma.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "projects")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;
    private String category;
    private LocalDateTime deadline;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToMany
    @JoinTable(
        name = "project_participants",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (participants == null) {
            participants = new HashSet<>();
        }
    }
}
