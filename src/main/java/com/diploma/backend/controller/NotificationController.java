package com.diploma.backend.controller;

import com.diploma.backend.dto.CreateNotificationRequest;
import com.diploma.backend.dto.NotificationDto;
import com.diploma.backend.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        return new ResponseEntity<>(notificationService.createNotification(request), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<NotificationDto>> getUserNotifications(@PathVariable UUID userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(@PathVariable UUID userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    @PostMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('ADMIN') or @notificationService.getUserNotifications(authentication.principal.id).stream().anyMatch(n -> n.getId().equals(#notificationId))")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/unread-count")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<Long> getUnreadCount(@PathVariable UUID userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }
} 
