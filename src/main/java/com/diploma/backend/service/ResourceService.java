package com.diploma.backend.service;

import com.diploma.backend.exception.ResourceNotFoundException;
import com.diploma.backend.dto.CreateResourceRequest;
import com.diploma.backend.dto.ResourceDto;
import com.diploma.backend.entity.Group;
import com.diploma.backend.entity.Resource;
import com.diploma.backend.entity.Role;
import com.diploma.backend.entity.User;
import com.diploma.backend.repository.ResourceRepository;
import com.diploma.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<ResourceDto> listMy(Authentication auth) {
        User me = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

        if (me.getRoles().contains(Role.ADMIN)) {
            return resourceRepository.findAll().stream()
                    .map(ResourceDto::fromEntity)
                    .collect(Collectors.toList());
        }

        Role myRole = me.getRoles().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("У пользователя нет роли"));

        List<UUID> myGroupIds = me.getGroups().stream()
                .map(Group::getId)
                .collect(Collectors.toList());

        return resourceRepository
                .findAccessibleResourcesByRoleOrGroups(myRole, myGroupIds)
                .stream()
                .map(ResourceDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResourceDto getById(UUID id, String email) {
        User me = userService.getByEmail(email);
        Role myRole = me.getRoles().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("У пользователя нет роли"));

        List<UUID> myGroupIds = me.getGroups().stream()
                .map(Group::getId)
                .collect(Collectors.toList());

        // Получаем ресурс только если он доступен по роли или группам
        Resource resource = resourceRepository
                .findAccessibleResourcesByRoleOrGroups(myRole, myGroupIds)
                .stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Access denied"));

        return ResourceDto.fromEntity(resource);
    }


    @Transactional
    public ResourceDto update(UUID id, CreateResourceRequest req, String userEmail) {
        User me = userService.getByEmail(userEmail);
        // только STAFF и ADMIN могут редактировать
        boolean canEdit = me.getRoles().contains(Role.ADMIN) || me.getRoles().contains(Role.STAFF);
        if (!canEdit) {
            throw new AccessDeniedException("Только STAFF и ADMIN могут изменять ресурсы");
        }

        Resource r = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));

        // обновляем поля
        r.setName(req.getName());
        r.setType(req.getType());
        r.setUrl(URI.create(req.getUrl()).toString());
        r.setAllowedRoles(req.getAllowedRoles());
        r.setAllowedGroupIds(req.getAllowedGroupIds());

        Resource saved = resourceRepository.save(r);
        return ResourceDto.fromEntity(saved);
    }

    @Transactional
    public ResourceDto create(CreateResourceRequest req, String userEmail) {
        userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

        URI uri = URI.create(req.getUrl());
        Resource r = Resource.builder()
                .name(req.getName())
                .type(req.getType())
                .url(uri.toString())
                .allowedRoles(req.getAllowedRoles())
                .allowedGroupIds(req.getAllowedGroupIds())
                .build();

        return ResourceDto.fromEntity(resourceRepository.save(r));
    }

    @Transactional
    public void delete(UUID id, String userEmail) {
        User me = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

        boolean canDelete = me.getRoles().stream()
                .anyMatch(r -> r == Role.ADMIN || r == Role.STAFF);
        if (!canDelete) {
            throw new AccessDeniedException("Только STAFF и ADMIN могут удалять ресурсы");
        }

        resourceRepository.deleteById(id);
    }
}
