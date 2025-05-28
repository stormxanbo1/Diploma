// src/main/java/com/diploma/backend/service/UserService.java
package com.diploma.backend.service;

import com.diploma.backend.dto.*;
import com.diploma.backend.entity.Role;
import com.diploma.backend.entity.User;
import com.diploma.backend.exception.NotFoundException;
import com.diploma.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;


    /* ---------- CRUD ---------- */

    public UserDto create(CreateUserRequest req) {
        User u = User.builder()
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .roles(defaultRoles(req.getRoles()))
                .build();
        return toDto(userRepo.save(u));
    }

    public User getByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден: " + email));
    }


    public List<UserDto> listAll() {
        return userRepo.findAll()
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getById(UUID id) {
        return toDto(userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found")));
    }

    public UserDto update(UUID id, UpdateUserRequest req) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        u.setFirstName(req.getFirstName());
        u.setLastName(req.getLastName());
        return toDto(userRepo.save(u));
    }

    public void delete(UUID id) {
        if (!userRepo.existsById(id)) {
            throw new NotFoundException("User not found");
        }
        userRepo.deleteById(id);
    }

    /* ---------- роли ---------- */

    public UserDto updateRoles(UUID id, RoleUpdateRequest req) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        u.setRoles(defaultRoles(req.getRoles()));
        return toDto(userRepo.save(u));
    }

    /* ---------- helpers ---------- */

    private UserDto toDto(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setFirstName(u.getFirstName());
        dto.setLastName(u.getLastName());
        dto.setRoles(u.getRoles());
        return dto;
    }

    private Set<Role> defaultRoles(Set<Role> roles) {
        return (roles == null || roles.isEmpty())
                ? Set.of(Role.STUDENT)
                : roles;
    }
}