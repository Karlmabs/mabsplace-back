package com.mabsplace.mabsplaceback.domain.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mabsplace.mabsplaceback.domain.controllers.WebSocketNotificationController;
import com.mabsplace.mabsplaceback.domain.dtos.notification.NotificationDTO;
import com.mabsplace.mabsplaceback.domain.entities.DigitalGoodsOrder;
import com.mabsplace.mabsplaceback.domain.entities.Notification;
import com.mabsplace.mabsplaceback.domain.entities.Payment;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.enums.NotificationType;
import com.mabsplace.mabsplaceback.domain.enums.PaymentStatus;
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

    @Autowired
    private WebSocketNotificationController webSocketNotificationController;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public List<Notification> getUserNotifications(String username) {
        logger.info("Getting notifications for user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        logger.info("User found: {}", user.getEmail());
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Long getUnreadCount(Long userId) {
        logger.info("Getting unread notification count for user ID: {}", userId);
        Long count = notificationRepository.countUnreadNotifications(userId);
        logger.info("Unread count for user ID {}: {}", userId, count);
        return count;
    }

    public User getUserByUsername(String username) {
        logger.info("Getting user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public void markAsRead(Long notificationId, String username) {
        logger.info("Marking notification as read: {}", notificationId);

        User user = userRepository.findByUsername(username)
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


    public void markAllAsRead(String username) {
        logger.info("Marking all notifications as read for user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        logger.info("User found: {}", user.getEmail());
        logger.info("Marking all notifications as read");

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);

        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    public void updateUserPushToken(String username, String pushToken) {
        logger.info("Updating push token for user: {}", username);
        User user = userRepository.findByUsername(username)
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

    // ==================== ADMIN NOTIFICATION METHODS ====================

    /**
     * Get all users with admin roles or specific permissions
     * @param permissionCode Optional permission code to filter admins (e.g., "GET_DIGITAL_GOODS_ORDERS")
     * @return List of admin users
     */
    public List<User> getAdminsByPermission(String permissionCode) {
        logger.info("Getting admins with permission: {}", permissionCode);

        // Get all users with ROLE_ADMIN or users who have the specific permission
        List<User> allUsers = userRepository.findAll();

        List<User> admins = allUsers.stream()
                .filter(user -> user.getUserProfile() != null &&
                        user.getUserProfile().getRoles().stream()
                                .anyMatch(role ->
                                        // Check if user has admin role OR the specific permission
                                        role.getCode().equals("ROLE_ADMIN") ||
                                        (permissionCode != null && role.getCode().equals(permissionCode))
                                )
                )
                .collect(Collectors.toList());

        logger.info("Found {} admins with permission: {}", admins.size(), permissionCode);
        return admins;
    }

    /**
     * Convert Notification entity to NotificationDTO
     */
    private NotificationDTO convertToNotificationDTO(Notification notification) {
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

    /**
     * Notify admins of a new digital goods order
     * @param order The newly created order
     */
    @Async
    public void notifyAdminsOfNewDigitalGoodsOrder(DigitalGoodsOrder order) {
        try {
            logger.info("Notifying admins of new digital goods order ID: {}", order.getId());

            // Get admins with permission to view digital goods orders
            List<User> admins = getAdminsByPermission("GET_DIGITAL_GOODS_ORDERS");

            if (admins.isEmpty()) {
                logger.warn("No admins found with GET_DIGITAL_GOODS_ORDERS permission");
                return;
            }

            // Create notification data
            Map<String, Object> data = new HashMap<>();
            data.put("type", "SYSTEM");
            data.put("orderId", order.getId());
            data.put("productName", order.getProduct().getName());
            data.put("amount", order.getAmount().toString());
            data.put("totalAmount", order.getTotalAmount().toString());
            data.put("customerName", order.getUser().getName());

            String title = "New Digital Goods Order";
            String message = String.format("%s ordered %s %s (Total: %s XAF)",
                    order.getUser().getUsername(),
                    order.getAmount(),
                    order.getProduct().getName(),
                    order.getTotalAmount());

            // Save notifications to database for each admin
            List<Notification> notifications = admins.stream()
                    .map(admin -> createNotification(admin, title, message, data))
                    .collect(Collectors.toList());

            notificationRepository.saveAll(notifications);
            logger.info("Saved {} notifications to database for new digital goods order", notifications.size());

            // Send WebSocket notifications to all admins
            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .id(notifications.get(0).getId())
                    .title(title)
                    .message(message)
                    .type("SYSTEM")
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .data(objectMapper.writeValueAsString(data))
                    .build();

            webSocketNotificationController.sendNotificationToAdmins(notificationDTO);
            logger.info("WebSocket notification sent to admins for digital goods order ID: {}", order.getId());

        } catch (Exception e) {
            logger.error("Failed to notify admins of new digital goods order ID: {}", order.getId(), e);
        }
    }

    /**
     * Notify admins of a new payment/subscription
     * @param payment The newly created payment
     */
    @Async
    public void notifyAdminsOfNewPayment(Payment payment) {
        try {
            logger.info("Notifying admins of new payment ID: {}", payment.getId());

            // Get admins with permission to view payments
            List<User> admins = getAdminsByPermission("GET_PAYMENTS");

            if (admins.isEmpty()) {
                logger.warn("No admins found with GET_PAYMENTS permission");
                return;
            }

            // Create notification data
            Map<String, Object> data = new HashMap<>();
            data.put("type", "PAYMENT");
            data.put("paymentId", payment.getId());
            data.put("amount", payment.getAmount().toString());
            data.put("customerName", payment.getUser().getName());
            if (payment.getService() != null) {
                data.put("serviceName", payment.getService().getName());
            }
            if (payment.getSubscriptionPlan() != null) {
                data.put("planName", payment.getSubscriptionPlan().getName());
            }

            String title = "New Payment Received";
            String message = String.format("%s paid %s %s for %s",
                    payment.getUser().getUsername(),
                    payment.getAmount(),
                    payment.getCurrency() != null ? payment.getCurrency().getName() : "",
                    payment.getService() != null ? payment.getService().getName() : "subscription");

            // Save notifications to database for each admin
            List<Notification> notifications = admins.stream()
                    .map(admin -> createNotification(admin, title, message, data))
                    .collect(Collectors.toList());

            notificationRepository.saveAll(notifications);
            logger.info("Saved {} notifications to database for new payment", notifications.size());

            // Send WebSocket notifications to all admins
            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .id(notifications.get(0).getId())
                    .title(title)
                    .message(message)
                    .type("PAYMENT")
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .data(objectMapper.writeValueAsString(data))
                    .build();

            webSocketNotificationController.sendNotificationToAdmins(notificationDTO);
            logger.info("WebSocket notification sent to admins for payment ID: {}", payment.getId());

        } catch (Exception e) {
            logger.error("Failed to notify admins of new payment ID: {}", payment.getId(), e);
        }
    }

    /**
     * Notify admins of an order status change
     * @param order The updated order
     * @param oldStatus The previous status
     */
    @Async
    public void notifyAdminsOfOrderStatusChange(DigitalGoodsOrder order, DigitalGoodsOrder.OrderStatus oldStatus) {
        try {
            logger.info("Notifying admins of order status change for order ID: {}, {} -> {}",
                    order.getId(), oldStatus, order.getOrderStatus());

            // Get admins with permission to manage digital orders
            List<User> admins = getAdminsByPermission("MANAGE_DIGITAL_ORDERS");

            if (admins.isEmpty()) {
                logger.warn("No admins found with MANAGE_DIGITAL_ORDERS permission");
                return;
            }

            // Create notification data
            Map<String, Object> data = new HashMap<>();
            data.put("type", "SYSTEM");
            data.put("orderId", order.getId());
            data.put("oldStatus", oldStatus.name());
            data.put("newStatus", order.getOrderStatus().name());
            data.put("productName", order.getProduct().getName());
            data.put("customerName", order.getUser().getName());

            String title = "Order Status Updated";
            String message = String.format("Order #%d status changed from %s to %s",
                    order.getId(), oldStatus, order.getOrderStatus());

            // Save notifications to database for each admin
            List<Notification> notifications = admins.stream()
                    .map(admin -> createNotification(admin, title, message, data))
                    .collect(Collectors.toList());

            notificationRepository.saveAll(notifications);

            // Send WebSocket notifications to all admins
            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .id(notifications.get(0).getId())
                    .title(title)
                    .message(message)
                    .type("SYSTEM")
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .data(objectMapper.writeValueAsString(data))
                    .build();

            webSocketNotificationController.sendNotificationToAdmins(notificationDTO);

        } catch (Exception e) {
            logger.error("Failed to notify admins of order status change for order ID: {}", order.getId(), e);
        }
    }

    /**
     * Notify admins of a payment status change
     * @param payment The updated payment
     * @param oldStatus The previous status
     */
    @Async
    public void notifyAdminsOfPaymentStatusChange(Payment payment, PaymentStatus oldStatus) {
        try {
            logger.info("Notifying admins of payment status change for payment ID: {}, {} -> {}",
                    payment.getId(), oldStatus, payment.getStatus());

            // Get admins with permission to manage payments
            List<User> admins = getAdminsByPermission("MANAGE_PAYMENTS");

            if (admins.isEmpty()) {
                logger.warn("No admins found with MANAGE_PAYMENTS permission");
                return;
            }

            // Create notification data
            Map<String, Object> data = new HashMap<>();
            data.put("type", "PAYMENT");
            data.put("paymentId", payment.getId());
            data.put("oldStatus", oldStatus.name());
            data.put("newStatus", payment.getStatus().name());
            data.put("customerName", payment.getUser().getName());
            data.put("amount", payment.getAmount().toString());

            String title = "Payment Status Updated";
            String message = String.format("Payment #%d status changed from %s to %s",
                    payment.getId(), oldStatus, payment.getStatus());

            // Save notifications to database for each admin
            List<Notification> notifications = admins.stream()
                    .map(admin -> createNotification(admin, title, message, data))
                    .collect(Collectors.toList());

            notificationRepository.saveAll(notifications);

            // Send WebSocket notifications to all admins
            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .id(notifications.get(0).getId())
                    .title(title)
                    .message(message)
                    .type("PAYMENT")
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .data(objectMapper.writeValueAsString(data))
                    .build();

            webSocketNotificationController.sendNotificationToAdmins(notificationDTO);

        } catch (Exception e) {
            logger.error("Failed to notify admins of payment status change for payment ID: {}", payment.getId(), e);
        }
    }

    @Async
    public void notifyAdminsOfNewSubscription(Subscription newSubscription) {
        try {
            logger.info("Notifying admins of new subscription ID: {}", newSubscription.getId());

            List<User> admins = getAdminsByPermission("GET_SUBSCRIPTIONS");

            if (admins.isEmpty()) {
                logger.warn("No admins found with GET_SUBSCRIPTIONS permission");
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("type", "SUBSCRIPTION");
            data.put("subscriptionId", newSubscription.getId());
            if (newSubscription.getUser() != null) {
                data.put("customerName", newSubscription.getUser().getName());
                data.put("customerId", newSubscription.getUser().getId());
            }
            if (newSubscription.getSubscriptionPlan() != null) {
                data.put("planName", newSubscription.getSubscriptionPlan().getName());
                data.put("planId", newSubscription.getSubscriptionPlan().getId());
            }
            if (newSubscription.getService() != null) {
                data.put("serviceName", newSubscription.getService().getName());
                data.put("serviceId", newSubscription.getService().getId());
            }

            String planName = newSubscription.getSubscriptionPlan() != null
                    ? newSubscription.getSubscriptionPlan().getName()
                    : "subscription";
            String serviceSuffix = newSubscription.getService() != null
                    ? String.format(" for %s", newSubscription.getService().getName())
                    : "";
            String customerName = newSubscription.getUser() != null
                    ? newSubscription.getUser().getUsername()
                    : "A user";

            String title = "New Subscription Created";
            String message = String.format("%s subscribed to %s%s", customerName, planName, serviceSuffix);

            List<Notification> notifications = admins.stream()
                    .map(admin -> createNotification(admin, title, message, data))
                    .collect(Collectors.toList());

            notificationRepository.saveAll(notifications);

            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .id(notifications.get(0).getId())
                    .title(title)
                    .message(message)
                    .type("SUBSCRIPTION")
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .data(objectMapper.writeValueAsString(data))
                    .build();

            webSocketNotificationController.sendNotificationToAdmins(notificationDTO);

        } catch (Exception e) {
            logger.error("Failed to notify admins of new subscription ID: {}", newSubscription.getId(), e);
        }
    }
}
