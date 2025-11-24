package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.notification.NotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controller for sending WebSocket notifications to connected clients
 */
@Controller
public class WebSocketNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Send notification to a specific user via WebSocket
     * @param userId The user ID to send the notification to
     * @param notification The notification DTO to send
     */
    public void sendNotificationToUser(Long userId, NotificationDTO notification) {
        try {
            logger.info("Sending WebSocket notification to user ID: {}, notification: {}", userId, notification.getTitle());

            // Send to user-specific destination
            // The client should subscribe to: /user/queue/notifications
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notification
            );

            logger.info("WebSocket notification sent successfully to user ID: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification to user ID: {}", userId, e);
        }
    }

    /**
     * Send notification to multiple users via WebSocket
     * @param userIds List of user IDs to send the notification to
     * @param notification The notification DTO to send
     */
    public void sendNotificationToUsers(List<Long> userIds, NotificationDTO notification) {
        logger.info("Sending WebSocket notification to {} users, notification: {}", userIds.size(), notification.getTitle());

        userIds.forEach(userId -> sendNotificationToUser(userId, notification));

        logger.info("WebSocket notification sent to {} users", userIds.size());
    }

    /**
     * Broadcast notification to all connected clients
     * @param notification The notification DTO to send
     */
    public void broadcastNotification(NotificationDTO notification) {
        try {
            logger.info("Broadcasting WebSocket notification to all users, notification: {}", notification.getTitle());

            // Send to topic that all clients can subscribe to
            // The client should subscribe to: /topic/notifications
            messagingTemplate.convertAndSend("/topic/notifications", notification);

            logger.info("WebSocket notification broadcasted successfully");
        } catch (Exception e) {
            logger.error("Failed to broadcast WebSocket notification", e);
        }
    }

    /**
     * Send notification specifically to admins
     * @param notification The notification DTO to send
     */
    public void sendNotificationToAdmins(NotificationDTO notification) {
        try {
            logger.info("Sending WebSocket notification to admins, notification: {}", notification.getTitle());

            // Send to admin-specific topic
            // Admin clients should subscribe to: /topic/admin/notifications
            logger.info("Attempting to send message to /topic/admin/notifications");
            messagingTemplate.convertAndSend("/topic/admin/notifications", notification);
            logger.info("Successfully sent message to /topic/admin/notifications");

            logger.info("WebSocket notification sent to admins successfully");
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification to admins", e);
        }
    }
}
