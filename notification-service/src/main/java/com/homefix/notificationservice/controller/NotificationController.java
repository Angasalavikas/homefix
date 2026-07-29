package com.homefix.notificationservice.controller;

import com.homefix.notificationservice.dto.NotificationRequest;
import com.homefix.notificationservice.dto.NotificationResponse;
import com.homefix.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * POST /internal/notifications — Internal endpoint for other services to create notifications.
     * Not exposed to the frontend. No JWT required (permitted in SecurityConfig).
     */
    @PostMapping("/internal/notifications")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /notifications — Get notifications for the authenticated user.
     * Optional ?unread=true filter.
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam(required = false) Boolean unread,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<NotificationResponse> responses = notificationService.getNotifications(userId, unread);
        return ResponseEntity.ok(responses);
    }

    /**
     * PUT /notifications/{id}/read — Mark a notification as read.
     */
    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        NotificationResponse response = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is up and running");
    }
}
