// src/main/java/com/diploma/backend/service/GroupService.java
package com.diploma.backend.service;

import com.diploma.backend.dto.CreateGroupRequest;
import com.diploma.backend.dto.GroupDto;
import com.diploma.backend.entity.Group;
import com.diploma.backend.entity.User;
import com.diploma.backend.exception.NotFoundException;
import com.diploma.backend.repository.GroupRepository;
import com.diploma.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepo;
    private final UserRepository userRepo;
    private final ModelMapper mapper;

    /* ---------- CRUD ---------- */

    public GroupDto create(CreateGroupRequest req, String creatorEmail) {
        User creator = userRepo.findByEmail(creatorEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Group g = Group.builder()
                .name(req.getName())
                .creator(creator)
                .build();

        // добавляем выбранных участников + создателя
        Set<User> members = new HashSet<>();
        if (req.getMemberIds() != null) {
            members.addAll(userRepo.findAllById(req.getMemberIds()));
        }
        members.add(creator);
        g.setMembers(members);

        return toDto(groupRepo.save(g));
    }

    /** Мои группы (созданные мною или где я участник). ADMIN/STAFF — видят все */
    public List<GroupDto> listMy(String userEmail) {
        boolean isStaffOrAdmin = userRepo.findByEmail(userEmail)
                .map(u -> u.getRoles().stream()
                        .anyMatch(r -> r.name().equals("STAFF") || r.name().equals("ADMIN")))
                .orElse(false);

        if (isStaffOrAdmin) {
            return groupRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
        }

        UUID myId = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getId();

        Set<Group> result = new HashSet<>(groupRepo.findByCreator_Email(userEmail));
        result.addAll(groupRepo.findByMembers_Id(myId));

        return result.stream().map(this::toDto).collect(Collectors.toList());
    }

    public GroupDto getById(UUID id, String userEmail) {
        Group g = groupRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        checkAccess(g, userEmail);
        return toDto(g);
    }

    public GroupDto update(UUID id, CreateGroupRequest req, String userEmail) {
        Group g = groupRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        checkAccess(g, userEmail);

        g.setName(req.getName());

        if (req.getMemberIds() != null) {
            Set<User> members = new HashSet<>(userRepo.findAllById(req.getMemberIds()));
            // гарантируем, что создатель остаётся в группе
            members.add(g.getCreator());
            g.setMembers(members);
        }

        return toDto(groupRepo.save(g));
    }

    public void delete(UUID id, String userEmail) {
        Group g = groupRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        checkAccess(g, userEmail);
        groupRepo.delete(g);
    }

    /* ---------- участники ---------- */

    public GroupDto addMembers(UUID id, Set<UUID> memberIds, String userEmail) {
        Group g = groupRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        checkAccess(g, userEmail);

        g.getMembers().addAll(userRepo.findAllById(memberIds));
        return toDto(groupRepo.save(g));
    }

    public GroupDto removeMember(UUID id, UUID memberId, String userEmail) {
        Group g = groupRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        checkAccess(g, userEmail);

        boolean removed = g.getMembers().removeIf(u -> u.getId().equals(memberId));
        if (!removed) {
            throw new NotFoundException("User is not a member of this group");
        }
        return toDto(groupRepo.save(g));
    }

    /* ---------- helpers ---------- */

    private void checkAccess(Group g, String userEmail) {
        boolean isCreator = g.getCreator().getEmail().equals(userEmail);
        boolean isStaffOrAdmin = userRepo.findByEmail(userEmail)
                .map(u -> u.getRoles().stream()
                        .anyMatch(r -> r.name().equals("STAFF") || r.name().equals("ADMIN")))
                .orElse(false);
        if (!isCreator && !isStaffOrAdmin) {
            throw new AccessDeniedException("Доступ запрещён");
        }
    }

    private GroupDto toDto(Group g) {
        GroupDto dto = mapper.map(g, GroupDto.class);
        dto.setCreatorId(g.getCreator().getId());
        dto.setMemberIds(g.getMembers().stream()
                .map(User::getId)
                .collect(Collectors.toSet()));
        return dto;
    }
}
