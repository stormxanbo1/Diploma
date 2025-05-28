package com.diploma.backend.service;

import com.diploma.backend.exception.ResourceNotFoundException;
import com.diploma.backend.dto.CreateResourceRequest;
import com.diploma.backend.dto.ResourceDto;
import com.diploma.backend.entity.Group;
import com.diploma.backend.entity.Resource;
import com.diploma.backend.entity.User;
import com.diploma.backend.repository.ResourceRepository;
import com.diploma.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
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

        log.debug("User: {}, Groups: {}", me.getEmail(), me.getGroups().size());

        List<UUID> myGroupIds = me.getGroups().stream()
                .map(group -> group.getId())
                .collect(Collectors.toList());

        log.debug("User group IDs: {}", myGroupIds);

        // Используем новый метод для получения доступных ресурсов
        List<Resource> resources = getAccessibleResources(myGroupIds);

        log.debug("Found {} resources", resources.size());

        // Логируем каждый ресурс для отладки
        resources.forEach(r ->
                log.debug("Resource: id={}, name={}, allowedGroups={}",
                        r.getId(), r.getName(), r.getAllowedGroupIds())
        );

        return resources.stream()
                .map(ResourceDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResourceDto getById(UUID id, String email) {
        User me = userService.getByEmail(email);

        List<UUID> myGroupIds = me.getGroups().stream()
                .map(group -> group.getId())
                .collect(Collectors.toList());

        // Используем новый метод для получения доступных ресурсов
        Resource resource = getAccessibleResources(myGroupIds)
                .stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found or access denied"));

        return ResourceDto.fromEntity(resource);
    }

    @Transactional
    public ResourceDto update(UUID id, CreateResourceRequest req, String userEmail) {
        Resource r = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));

        r.setName(req.getName());
        r.setType(req.getType());
        r.setUrl(URI.create(req.getUrl()).toString());
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
                .allowedGroupIds(req.getAllowedGroupIds())
                .build();

        return ResourceDto.fromEntity(resourceRepository.save(r));
    }

    @Transactional
    public void delete(UUID id, String userEmail) {
        resourceRepository.deleteById(id);
    }

    /**
     * Метод для получения доступных ресурсов с правильной обработкой пустых групп
     */
    private List<Resource> getAccessibleResources(List<UUID> groupIds) {
        // Если список групп пустой или null, возвращаем только ресурсы без ограничений по группам
        if (groupIds == null || groupIds.isEmpty()) {
            log.debug("User has no groups, fetching resources without group restrictions");
            return resourceRepository.findResourcesWithoutGroupRestrictions();
        }

        log.debug("Fetching resources accessible to groups: {}", groupIds);
        return resourceRepository.findAccessibleResourcesByGroups(groupIds);
    }
}