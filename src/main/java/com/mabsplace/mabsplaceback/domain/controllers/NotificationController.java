package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.notification.ApiResponse;
import com.mabsplace.mabsplaceback.domain.dtos.notification.NotificationDTO;
import com.mabsplace.mabsplaceback.domain.dtos.notification.PushNotificationRequest;
import com.mabsplace.mabsplaceback.domain.dtos.notification.UpdatePushTokenRequest;
import com.mabsplace.mabsplaceback.domain.entities.Notification;
import com.mabsplace.mabsplaceback.domain.services.NotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    /**
     * Update user's push notification token
     */
    @PostMapping("/token")
    public ResponseEntity<ApiResponse> updatePushToken(
            @RequestBody @Valid UpdatePushTokenRequest request,
            Authentication authentication
    ) {
        logger.info("Updating push token for user: {}", authentication.getName());
        try {
            notificationService.updateUserPushToken(authentication.getName(), request.getPushToken());
            logger.info("Push token updated successfully for user: {}", authentication.getName());
            return ResponseEntity.ok(new ApiResponse(true, "Push token updated successfully"));
        } catch (Exception e) {
            logger.error("Failed to update push token for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to update push token: " + e.getMessage()));
        }
    }

    /**
     * Get user's notifications
     */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean unreadOnly
    ) {
        logger.info("Fetching notifications for user: {}, page: {}, size: {}, unreadOnly: {}", authentication.getName(), page, size, unreadOnly);
        List<Notification> notifications = notificationService.getUserNotifications(authentication.getName());

        List<NotificationDTO> notificationDTOs = notifications.stream()
                .filter(n -> unreadOnly == null || !unreadOnly || !n.isRead())
                .skip((long) page * size)
                .limit(size)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        logger.info("Fetched {} notifications for user: {}", notificationDTOs.size(), authentication.getName());
        return ResponseEntity.ok(notificationDTOs);
    }

    /**
     * Mark a notification as read
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        logger.info("Marking notification as read, ID: {}, user: {}", notificationId, authentication.getName());
        try {
            notificationService.markAsRead(notificationId, authentication.getName());
            logger.info("Notification marked as read successfully, ID: {}", notificationId);
            return ResponseEntity.ok(new ApiResponse(true, "Notification marked as read"));
        } catch (Exception e) {
            logger.error("Failed to mark notification as read, ID: {}", notificationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to mark notification as read: " + e.getMessage()));
        }
    }

    /**
     * Mark all notifications as read
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<ApiResponse> markAllAsRead(Authentication authentication) {
        logger.info("Marking all notifications as read for user: {}", authentication.getName());
        try {
            notificationService.markAllAsRead(authentication.getName());
            logger.info("All notifications marked as read for user: {}", authentication.getName());
            return ResponseEntity.ok(new ApiResponse(true, "All notifications marked as read"));
        } catch (Exception e) {
            logger.error("Failed to mark all notifications as read for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to mark notifications as read: " + e.getMessage()));
        }
    }

    /**
     * Send notification to specific users (Admin only)
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse> sendNotification(
            @RequestBody @Valid PushNotificationRequest request
    ) {
        logger.info("Sending notifications to users: {}", request.getUserIds());
        try {
            notificationService.sendPushNotification(
                    request.getUserIds(),
                    request.getTitle(),
                    request.getBody(),
                    request.getData()
            );
            logger.info("Notifications sent successfully to users: {}", request.getUserIds());
            return ResponseEntity.ok(new ApiResponse(true, "Notifications sent successfully"));
        } catch (Exception e) {
            logger.error("Failed to send notifications to users: {}", request.getUserIds(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to send notifications: " + e.getMessage()));
        }
    }

    /**
     * Send notification to all users (Admin only)
     */
    @PostMapping("/send-all")
    public ResponseEntity<ApiResponse> sendNotificationToAllUsers(
            @RequestBody @Valid PushNotificationRequest request
    ) {
        logger.info("Sending notification to all users: {}", request.getTitle());
        try {
            notificationService.sendNotificationToAllUsers(
                    request.getTitle(),
                    request.getBody(),
                    request.getData()
            );
            logger.info("Notification sent successfully to all users: {}", request.getTitle());
            return ResponseEntity.ok(new ApiResponse(true, "Notifications sent to all users successfully"));
        } catch (Exception e) {
            logger.error("Failed to send notifications to all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to send notifications: " + e.getMessage()));
        }
    }

    // Helper method to convert Notification to DTO
    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .data(notification.getData())
                .build();
    }
}