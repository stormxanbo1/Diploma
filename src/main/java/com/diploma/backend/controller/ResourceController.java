package com.diploma.backend.controller;

import com.diploma.backend.dto.CreateResourceRequest;
import com.diploma.backend.dto.ResourceDto;
import com.diploma.backend.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService service;

    @GetMapping
    public List<ResourceDto> listMy(Authentication auth) {
        return service.listMy(auth);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDto> getById(
            @PathVariable UUID id,
            Authentication auth
    ) {
        return ResponseEntity.ok(service.getById(id, auth.getName()));
    }

    @PostMapping
    public ResponseEntity<ResourceDto> create(
            @Valid @RequestBody CreateResourceRequest req,
            Authentication auth
    ) {
        ResourceDto dto = service.create(req, auth.getName());
        URI location = URI.create("/api/resources/" + dto.getId());
        return ResponseEntity.created(location).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateResourceRequest req,
            Authentication auth
    ) {
        ResourceDto dto = service.update(id, req, auth.getName());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication auth
    ) {
        service.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
