package com.diploma.backend.repository;

import com.diploma.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);
    
    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(UUID recipientId, boolean isRead);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notificationId")
    void markAsRead(UUID notificationId);
    
    long countByRecipientIdAndIsRead(UUID recipientId, boolean isRead);
} 