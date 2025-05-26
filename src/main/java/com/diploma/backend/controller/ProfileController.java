package com.diploma.backend.controller;

import com.diploma.backend.dto.ProfileDto;
import com.diploma.backend.entity.User;
import com.diploma.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final UserRepository userRepo;

    @GetMapping
    public ResponseEntity<ProfileDto> getProfile(Authentication auth) {
        User u = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        ProfileDto d = new ProfileDto();
        d.setId(u.getId());
        d.setEmail(u.getEmail());
        d.setFirstName(u.getFirstName());
        d.setLastName(u.getLastName());
        d.setRoles(u.getRoles().stream().map(Enum::name).collect(Collectors.toList()));
        return ResponseEntity.ok(d);
    }
}
