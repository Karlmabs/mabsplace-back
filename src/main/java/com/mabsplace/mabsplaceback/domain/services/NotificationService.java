package com.mabsplace.mabsplaceback.domain.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mabsplace.mabsplaceback.domain.entities.Notification;
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

    public List<Notification> getUserNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void markAsRead(Long notificationId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        notificationRepository.markAllAsRead(user.getId());
    }

    public void updateUserPushToken(String email, String pushToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setPushToken(pushToken);
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
            List<User> users = userRepository.findAllById(userIds);
            List<String> pushTokens = users.stream()
                    .map(User::getPushToken)
                    .filter(Objects::nonNull)
                    .toList();

            if (pushTokens.isEmpty()) {
                return;
            }

            // Save notifications to database
            List<Notification> notifications = users.stream()
                    .map(user -> createNotification(user, title, body, data))
                    .collect(Collectors.toList());
            notificationRepository.saveAll(notifications);

            // Prepare and send push notifications
            List<Map<String, Object>> messages = pushTokens.stream()
                    .map(token -> createPushMessage(token, title, body, data))
                    .collect(Collectors.toList());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<Map<String, Object>>> request =
                    new HttpEntity<>(messages, headers);

            restTemplate.postForObject(expoPushApiUrl, request, String.class);

        } catch (Exception e) {
            // Log the error and handle it appropriately
            e.printStackTrace();
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
        sendPushNotification(Collections.singletonList(userId), title, body, data);
    }

    // Method to send notification to all users
    @Async
    public void sendNotificationToAllUsers(
            String title,
            String body,
            Map<String, Object> data
    ) {
        List<Long> allUserIds = userRepository.findAll().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        sendPushNotification(allUserIds, title, body, data);
    }
}