package com.diploma.backend.service;

import com.diploma.backend.dto.CreateNotificationRequest;
import com.diploma.backend.dto.NotificationDto;
import com.diploma.backend.entity.Notification;
import com.diploma.backend.entity.User;
import com.diploma.backend.exception.ResourceNotFoundException;
import com.diploma.backend.repository.NotificationRepository;
import com.diploma.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationDto createNotification(CreateNotificationRequest request) {
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getRecipientId()));

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());

        notification = notificationRepository.save(notification);
        return convertToDto(notification);
    }

    public List<NotificationDto> getUserNotifications(UUID userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDto> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(userId, false)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + notificationId);
        }
        notificationRepository.markAsRead(notificationId);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByRecipientIdAndIsRead(userId, false);
    }

    private NotificationDto convertToDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getRecipient().getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.isRead(),
                notification.getType()
        );
    }
} 
