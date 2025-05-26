package com.diploma.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "resources")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Resource {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    private String name;
    private String type;
    private String url;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "resource_allowed_roles", joinColumns = @JoinColumn(name = "resource_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> allowedRoles;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "resource_allowed_groups", joinColumns = @JoinColumn(name = "resource_id"))
    private Set<UUID> allowedGroupIds;
}
