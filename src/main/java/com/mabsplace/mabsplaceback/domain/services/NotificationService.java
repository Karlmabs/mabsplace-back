package com.mabsplace.mabsplaceback.domain.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mabsplace.mabsplaceback.domain.entities.Notification;
import com.mabsplace.mabsplaceback.domain.entities.PackageSubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.entities.ServicePackage;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.enums.NotificationType;
import com.mabsplace.mabsplaceback.domain.repositories.NotificationRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Value("${expo.push.api.url:https://exp.host/--/api/v2/push/send}")
    private String expoPushApiUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public List<Notification> getUserNotifications(String email) {
        logger.info("Getting notifications for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        logger.info("User found: {}", user.getEmail());
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void markAsRead(Long notificationId, String email) {
        logger.info("Marking notification as read: {}", notificationId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        logger.info("User found: {}", user.getEmail());

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        logger.info("Notification found: {}", notification.getId());

        if (!notification.getUser().getId().equals(user.getId())) {
            logger.warn("Unauthorized access to notification");
            throw new RuntimeException("Unauthorized access to notification");
        }

        notification.setRead(true);
        logger.info("Notification marked as read: {}", notification.getId());
        logger.info("Saving notification to database");
        notificationRepository.save(notification);
    }


    public void markAllAsRead(String email) {
        logger.info("Marking all notifications as read for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        logger.info("User found: {}", user.getEmail());
        logger.info("Marking all notifications as read");

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);

        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    public void updateUserPushToken(String email, String pushToken) {
        logger.info("Updating push token for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        logger.info("User found: {}", user.getEmail());
        user.setPushToken(pushToken);
        logger.info("Saving push token to database");
        userRepository.save(user);
    }

    @Async
    public void sendPushNotification(
            List<Long> userIds,
            String title,
            String body,
            Map<String, Object> data
    ) {
        try {
            logger.info("Initiating push notification for user IDs: {}", userIds);
            List<User> users = userRepository.findAllById(userIds);
            logger.info("Found {} users for notification", users.size());
            List<String> pushTokens = users.stream()
                    .map(User::getPushToken)
                    .filter(Objects::nonNull)
                    .toList();

            // Removed redundant push token count log as per structured logging requirements

            if (pushTokens.isEmpty()) {
                logger.warn("No push tokens found for the provided user IDs: {}", userIds);
                return;
            }

            logger.info("Preparing to send push notifications");
            // Save notifications to database
            List<Notification> notifications = users.stream()
                    .map(user -> createNotification(user, title, body, data))
                    .collect(Collectors.toList());
            logger.info("Saving {} notifications to database", notifications.size());
            notificationRepository.saveAll(notifications);

            logger.info("Sending push notifications");
            // Prepare and send push notifications
            List<Map<String, Object>> messages = pushTokens.stream()
                    .map(token -> createPushMessage(token, title, body, data))
                    .collect(Collectors.toList());

            logger.debug("Prepared push notifications: {}", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<Map<String, Object>>> request =
                    new HttpEntity<>(messages, headers);

            String s = restTemplate.postForObject(expoPushApiUrl, request, String.class);

            logger.info("Push notifications sent successfully. Response: {}", s);

        } catch (Exception e) {
            logger.error("Failed to send push notifications to users: {}. Error: {}", userIds, e.getMessage(), e);
        }
    }

    private Notification createNotification(
            User user,
            String title,
            String message,
            Map<String, Object> data
    ) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        // Set notification type based on data or default to SYSTEM
        String type = data.getOrDefault("type", "SYSTEM").toString();
        notification.setType(NotificationType.valueOf(type));

        try {
            notification.setData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            notification.setData("{}");
        }

        return notification;
    }

    private Map<String, Object> createPushMessage(
            String token,
            String title,
            String body,
            Map<String, Object> data
    ) {
        Map<String, Object> message = new HashMap<>();
        message.put("to", token);
        message.put("sound", "default");
        message.put("title", title);
        message.put("body", body);
        message.put("data", data);

        // Add additional Expo push notification options
        message.put("priority", "high");
        message.put("channelId", "default");
        return message;
    }

    // Method to send notification to a single user
    @Async
    public void sendNotificationToUser(
            Long userId,
            String title,
            String body,
            Map<String, Object> data
    ) {
        logger.info("Sending notification to user: " + userId);
        sendPushNotification(Collections.singletonList(userId), title, body, data);
    }

    // Method to send notification to all users
    @Async
    public void sendNotificationToAllUsers(
            String title,
            String body,
            Map<String, Object> data
    ) {
        logger.info("Sending notification to all users");
        List<Long> allUserIds = userRepository.findAll().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        logger.info("All users found: " + allUserIds.size());
        logger.info("Sending notification to all users");
        sendPushNotification(allUserIds, title, body, data);
    }

    public void notifyReferrerOfPromoCode(User referrer, String promoCode) {
        logger.info("Notifying referrer (ID: {}) of promo code: {}", referrer.getId(), promoCode);
        Map<String, Object> data = new HashMap<>();
        data.put("type", "PROMO_CODE");
        data.put("promoCode", promoCode);
        sendNotificationToUser(referrer.getId(), "Promo Code", "You have received a promo code", data);
        logger.info("Notification sent successfully to referrer (ID: {})", referrer.getId());
    }

    public void notifyUserOfNewPackageSubscription(User user, ServicePackage servicePackage, PackageSubscriptionPlan packagePlan) {
        logger.info("Notifying user (ID: {}) of new package subscription: {}", user.getId(), servicePackage.getName());
        Map<String, Object> data = new HashMap<>();
        data.put("type", "PACKAGE_SUBSCRIPTION");
        data.put("packageId", servicePackage.getId());
        data.put("packageName", servicePackage.getName());
        data.put("packagePlanId", packagePlan.getId());
        data.put("packagePlanName", packagePlan.getName());
        sendNotificationToUser(user.getId(), "New Subscription", "You have subscribed to a new package", data);
        logger.info("Notification sent successfully to user (ID: {})", user.getId());
    }
}