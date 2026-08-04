package com.homefix.notificationservice.service;

import com.homefix.notificationservice.dto.NotificationRequest;
import com.homefix.notificationservice.dto.NotificationResponse;
import com.homefix.notificationservice.entity.Notification;
import com.homefix.notificationservice.entity.NotificationType;
import com.homefix.notificationservice.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.homefix.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Create a notification (called internally by other services via Feign).
     */
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        NotificationType type;
        try {
            type = NotificationType.valueOf(request.getType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid notification type: " + request.getType());
        }

        Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .type(type)
                .message(request.getMessage())
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification created: type={}, recipientId={}", type, request.getRecipientId());

        return NotificationResponse.fromNotification(notification);
    }

    /**
     * Get notifications for the authenticated user, newest first.
     * Optional ?unread=true filter.
     */
    public List<NotificationResponse> getNotifications(Long recipientId, Boolean unreadOnly) {
        List<Notification> notifications;

        if (Boolean.TRUE.equals(unreadOnly)) {
            notifications = notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId);
        } else {
            notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
        }

        return notifications.stream()
                .map(NotificationResponse::fromNotification)
                .collect(Collectors.toList());
    }

    /**
     * Mark a notification as read.
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        if (!notification.getRecipientId().equals(userId)) {
            throw new AccessDeniedException("Notification does not belong to the authenticated user");
        }

        notification.setIsRead(true);
        notification = notificationRepository.save(notification);

        return NotificationResponse.fromNotification(notification);
    }
}
