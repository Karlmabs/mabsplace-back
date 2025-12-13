package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.notification.ApiResponse;
import com.mabsplace.mabsplaceback.domain.dtos.notification.NotificationDTO;
import com.mabsplace.mabsplaceback.domain.dtos.notification.PushNotificationRequest;
import com.mabsplace.mabsplaceback.domain.dtos.notification.UpdatePushTokenRequest;
import com.mabsplace.mabsplaceback.domain.entities.Notification;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.services.NotificationService;
import com.mabsplace.mabsplaceback.domain.services.OneSignalService;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private OneSignalService oneSignalService;

    @Autowired
    private UserRepository userRepository;

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
            logger.info("Updating push token for user: {}", authentication.getName());
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

    /**
     * Get unread notifications count for current user
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        logger.info("Fetching unread notification count for user: {}", authentication.getName());
        try {
            User user = notificationService.getUserByUsername(authentication.getName());
            Long count = notificationService.getUnreadCount(user.getId());
            logger.info("Unread notification count for user {}: {}", authentication.getName(), count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Failed to get unread count for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0L);
        }
    }

    /**
     * Register device for OneSignal push notifications
     */
    @PostMapping("/device/register")
    public ResponseEntity<ApiResponse> registerDevice(
            @RequestBody Map<String, String> request,
            Authentication authentication
    ) {
        logger.info("Registering device for OneSignal push notifications, user: {}", authentication.getName());
        try {
            String pushToken = request.get("pushToken");
            String deviceType = request.get("deviceType");

            if (pushToken == null || pushToken.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Push token is required"));
            }

            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update user's push token
            user.setPushToken(pushToken);
            userRepository.save(user);

            // Register/update user in OneSignal
            oneSignalService.createOrUpdateUser(
                    user.getId(),
                    user.getEmail(),
                    user.getPhonenumber(),
                    pushToken
            );

            logger.info("Device registered successfully for user: {}", authentication.getName());
            return ResponseEntity.ok(new ApiResponse(true, "Device registered successfully"));
        } catch (Exception e) {
            logger.error("Failed to register device for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to register device: " + e.getMessage()));
        }
    }

    /**
     * Get notification preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<Map<String, Boolean>> getNotificationPreferences(Authentication authentication) {
        logger.info("Fetching notification preferences for user: {}", authentication.getName());
        try {
            // TODO: Store preferences in database, for now return defaults
            Map<String, Boolean> preferences = new HashMap<>();
            preferences.put("email", true);
            preferences.put("push", true);
            preferences.put("sms", true);
            preferences.put("inApp", true);

            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            logger.error("Failed to fetch notification preferences for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>());
        }
    }

    /**
     * Update notification preferences
     */
    @PostMapping("/preferences")
    public ResponseEntity<ApiResponse> updateNotificationPreferences(
            @RequestBody Map<String, Boolean> preferences,
            Authentication authentication
    ) {
        logger.info("Updating notification preferences for user: {}, preferences: {}", authentication.getName(), preferences);
        try {
            // TODO: Save preferences to database
            // For now, just log them
            logger.info("Preferences updated: {}", preferences);

            return ResponseEntity.ok(new ApiResponse(true, "Preferences updated successfully"));
        } catch (Exception e) {
            logger.error("Failed to update notification preferences for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to update preferences: " + e.getMessage()));
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