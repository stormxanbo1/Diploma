// src/main/java/com/diploma/backend/controller/GroupController.java
package com.diploma.backend.controller;

import com.diploma.backend.dto.CreateGroupRequest;
import com.diploma.backend.dto.GroupDto;
import com.diploma.backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Validated
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupDto> create(
            @Valid @RequestBody CreateGroupRequest req,
            Authentication auth) {
        GroupDto dto = groupService.create(req, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public List<GroupDto> listMy(Authentication auth) {
        return groupService.listMy(auth.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getById(@PathVariable UUID id,
                                            Authentication auth) {
        GroupDto dto = groupService.getById(id, auth.getName());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateGroupRequest req,
            Authentication auth) {
        GroupDto dto = groupService.update(id, req, auth.getName());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       Authentication auth) {
        groupService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<GroupDto> addMembers(
            @PathVariable UUID id,
            @RequestBody Set<UUID> memberIds,
            Authentication auth) {
        GroupDto dto = groupService.addMembers(id, memberIds, auth.getName());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<GroupDto> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID memberId,
            Authentication auth) {
        GroupDto dto = groupService.removeMember(id, memberId, auth.getName());
        return ResponseEntity.ok(dto);
    }
}
