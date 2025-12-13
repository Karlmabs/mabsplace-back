package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.onesignal.OneSignalEmailRequest;
import com.mabsplace.mabsplaceback.domain.dtos.onesignal.OneSignalPushRequest;
import com.mabsplace.mabsplaceback.domain.dtos.onesignal.OneSignalSMSRequest;
import com.mabsplace.mabsplaceback.domain.entities.ServiceAccount;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationOrchestrator {
    private final OneSignalService oneSignalService;
    private final DiscordService discordService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Notify user when their subscription has been successfully renewed
     */
    @Async
    public void notifySubscriptionRenewed(Subscription subscription) {
        User user = subscription.getUser();
        String serviceName = subscription.getService().getName();
        String profileName = subscription.getProfile() != null ? subscription.getProfile().getProfileName() : "N/A";
        Date newEndDate = subscription.getEndDate();

        log.info("Sending subscription renewed notifications for user: {}, service: {}",
                user.getUsername(), serviceName);

        Map<String, String> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("service_name", serviceName);
        data.put("profile_name", profileName);
        data.put("new_end_date", DATE_FORMAT.format(newEndDate));

        // Email notification
        try {
            OneSignalEmailRequest emailRequest = OneSignalEmailRequest.builder()
                    .to(user.getEmail())
                    .subject("Subscription Renewed - " + serviceName)
                    .templateId("subscription-renewed")
                    .customData(data)
                    .build();
            oneSignalService.sendEmail(emailRequest);
        } catch (Exception e) {
            log.error("Failed to send renewal email to {}: {}", user.getEmail(), e.getMessage());
        }

        // Push notification (if user has push token)
        if (user.getPushToken() != null && !user.getPushToken().isEmpty()) {
            try {
                Map<String, Object> pushData = new HashMap<>();
                pushData.put("subscriptionId", subscription.getId());
                pushData.put("type", "renewed");
                pushData.put("serviceName", serviceName);

                OneSignalPushRequest pushRequest = OneSignalPushRequest.builder()
                        .externalUserId(user.getId().toString())
                        .title("Subscription Renewed")
                        .message("Your " + serviceName + " subscription has been renewed until " +
                                DATE_FORMAT.format(newEndDate))
                        .data(pushData)
                        .url("/subscriptions")
                        .build();
                oneSignalService.sendPushNotification(pushRequest);
            } catch (Exception e) {
                log.error("Failed to send renewal push notification to user {}: {}",
                        user.getId(), e.getMessage());
            }
        }

        // SMS notification
        try {
            OneSignalSMSRequest smsRequest = OneSignalSMSRequest.builder()
                    .phoneNumber(user.getPhonenumber())
                    .templateId("subscription_renewed")
                    .customData(data)
                    .build();
            oneSignalService.sendSMS(smsRequest);
        } catch (Exception e) {
            log.error("Failed to send renewal SMS to {}: {}", user.getPhonenumber(), e.getMessage());
        }

        // Discord admin notification
        try {
            discordService.sendSubscriptionRenewedNotification(
                    user.getUsername(),
                    serviceName,
                    DATE_FORMAT.format(newEndDate)
            );
        } catch (Exception e) {
            log.error("Failed to send Discord notification: {}", e.getMessage());
        }
    }

    /**
     * Notify user when their subscription is expiring soon
     */
    @Async
    public void notifySubscriptionExpiring(Subscription subscription, int daysRemaining) {
        User user = subscription.getUser();
        String serviceName = subscription.getService().getName();
        Date expiryDate = subscription.getEndDate();

        log.info("Sending subscription expiring notifications for user: {}, service: {}, days: {}",
                user.getUsername(), serviceName, daysRemaining);

        Map<String, String> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("service_name", serviceName);
        data.put("expiry_date", DATE_FORMAT.format(expiryDate));
        data.put("days_remaining", String.valueOf(daysRemaining));

        // Email notification
        try {
            OneSignalEmailRequest emailRequest = OneSignalEmailRequest.builder()
                    .to(user.getEmail())
                    .subject("Subscription Expiring Soon - " + serviceName)
                    .templateId("subscription-expiring")
                    .customData(data)
                    .build();
            oneSignalService.sendEmail(emailRequest);
        } catch (Exception e) {
            log.error("Failed to send expiring email to {}: {}", user.getEmail(), e.getMessage());
        }

        // Push notification
        if (user.getPushToken() != null && !user.getPushToken().isEmpty()) {
            try {
                Map<String, Object> pushData = new HashMap<>();
                pushData.put("subscriptionId", subscription.getId());
                pushData.put("type", "expiring");
                pushData.put("serviceName", serviceName);
                pushData.put("daysRemaining", daysRemaining);

                OneSignalPushRequest pushRequest = OneSignalPushRequest.builder()
                        .externalUserId(user.getId().toString())
                        .title("Subscription Expiring Soon")
                        .message("Your " + serviceName + " subscription expires in " + daysRemaining + " days")
                        .data(pushData)
                        .url("/subscriptions")
                        .build();
                oneSignalService.sendPushNotification(pushRequest);
            } catch (Exception e) {
                log.error("Failed to send expiring push notification to user {}: {}",
                        user.getId(), e.getMessage());
            }
        }

        // SMS notification
        try {
            OneSignalSMSRequest smsRequest = OneSignalSMSRequest.builder()
                    .phoneNumber(user.getPhonenumber())
                    .templateId("subscription_expiring_soon")
                    .customData(data)
                    .build();
            oneSignalService.sendSMS(smsRequest);
        } catch (Exception e) {
            log.error("Failed to send expiring SMS to {}: {}", user.getPhonenumber(), e.getMessage());
        }

        // Discord admin notification
        try {
            String accountLogin = subscription.getProfile() != null && subscription.getProfile().getServiceAccount() != null
                    ? subscription.getProfile().getServiceAccount().getLogin() : "N/A";
            String profileName = subscription.getProfile() != null
                    ? subscription.getProfile().getProfileName() : "N/A";

            discordService.sendSubscriptionExpiringNotification(
                    user.getUsername(),
                    serviceName,
                    DATE_FORMAT.format(expiryDate),
                    accountLogin,
                    profileName
            );
        } catch (Exception e) {
            log.error("Failed to send Discord notification: {}", e.getMessage());
        }
    }

    /**
     * Notify user when their subscription has expired
     */
    @Async
    public void notifySubscriptionExpired(Subscription subscription) {
        User user = subscription.getUser();
        String serviceName = subscription.getService().getName();
        String profileName = subscription.getProfile() != null ? subscription.getProfile().getProfileName() : "N/A";

        log.info("Sending subscription expired notifications for user: {}, service: {}",
                user.getUsername(), serviceName);

        Map<String, String> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("service_name", serviceName);
        data.put("profile_name", profileName);

        // Email notification
        try {
            OneSignalEmailRequest emailRequest = OneSignalEmailRequest.builder()
                    .to(user.getEmail())
                    .subject("Subscription Expired - " + serviceName)
                    .templateId("subscription-expired")
                    .customData(data)
                    .build();
            oneSignalService.sendEmail(emailRequest);
        } catch (Exception e) {
            log.error("Failed to send expired email to {}: {}", user.getEmail(), e.getMessage());
        }

        // Push notification
        if (user.getPushToken() != null && !user.getPushToken().isEmpty()) {
            try {
                Map<String, Object> pushData = new HashMap<>();
                pushData.put("subscriptionId", subscription.getId());
                pushData.put("type", "expired");
                pushData.put("serviceName", serviceName);

                OneSignalPushRequest pushRequest = OneSignalPushRequest.builder()
                        .externalUserId(user.getId().toString())
                        .title("Subscription Expired")
                        .message("Your " + serviceName + " subscription has expired")
                        .data(pushData)
                        .url("/subscriptions")
                        .build();
                oneSignalService.sendPushNotification(pushRequest);
            } catch (Exception e) {
                log.error("Failed to send expired push notification to user {}: {}",
                        user.getId(), e.getMessage());
            }
        }

        // SMS notification
        try {
            OneSignalSMSRequest smsRequest = OneSignalSMSRequest.builder()
                    .phoneNumber(user.getPhonenumber())
                    .templateId("subscription_expired")
                    .customData(data)
                    .build();
            oneSignalService.sendSMS(smsRequest);
        } catch (Exception e) {
            log.error("Failed to send expired SMS to {}: {}", user.getPhonenumber(), e.getMessage());
        }

        // Discord admin notification
        try {
            String accountLogin = subscription.getProfile() != null && subscription.getProfile().getServiceAccount() != null
                    ? subscription.getProfile().getServiceAccount().getLogin() : "N/A";

            discordService.sendSubscriptionExpiredNotification(
                    user.getUsername(),
                    serviceName,
                    accountLogin,
                    profileName
            );
        } catch (Exception e) {
            log.error("Failed to send Discord notification: {}", e.getMessage());
        }
    }

    /**
     * Notify user when subscription renewal has failed
     */
    @Async
    public void notifyRenewalFailed(Subscription subscription, int attemptNumber) {
        User user = subscription.getUser();
        String serviceName = subscription.getService().getName();

        log.info("Sending renewal failed notifications for user: {}, service: {}, attempt: {}",
                user.getUsername(), serviceName, attemptNumber);

        Map<String, String> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("service_name", serviceName);
        data.put("attempt_number", String.valueOf(attemptNumber));

        // Email notification
        try {
            OneSignalEmailRequest emailRequest = OneSignalEmailRequest.builder()
                    .to(user.getEmail())
                    .subject("Subscription Renewal Failed - " + serviceName)
                    .templateId("renewal-failed")
                    .customData(data)
                    .build();
            oneSignalService.sendEmail(emailRequest);
        } catch (Exception e) {
            log.error("Failed to send renewal failed email to {}: {}", user.getEmail(), e.getMessage());
        }

        // Push notification
        if (user.getPushToken() != null && !user.getPushToken().isEmpty()) {
            try {
                Map<String, Object> pushData = new HashMap<>();
                pushData.put("subscriptionId", subscription.getId());
                pushData.put("type", "renewal_failed");
                pushData.put("serviceName", serviceName);
                pushData.put("attemptNumber", attemptNumber);

                OneSignalPushRequest pushRequest = OneSignalPushRequest.builder()
                        .externalUserId(user.getId().toString())
                        .title("Renewal Failed")
                        .message("Failed to renew your " + serviceName + " subscription (Attempt " +
                                attemptNumber + ")")
                        .data(pushData)
                        .url("/subscriptions")
                        .build();
                oneSignalService.sendPushNotification(pushRequest);
            } catch (Exception e) {
                log.error("Failed to send renewal failed push notification to user {}: {}",
                        user.getId(), e.getMessage());
            }
        }

        // SMS notification
        try {
            OneSignalSMSRequest smsRequest = OneSignalSMSRequest.builder()
                    .phoneNumber(user.getPhonenumber())
                    .templateId("subscription_renewal_failed")
                    .customData(data)
                    .build();
            oneSignalService.sendSMS(smsRequest);
        } catch (Exception e) {
            log.error("Failed to send renewal failed SMS to {}: {}", user.getPhonenumber(), e.getMessage());
        }

        // Discord admin notification
        try {
            discordService.sendSubscriptionRenewalFailedNotification(
                    user.getUsername(),
                    serviceName,
                    attemptNumber
            );
        } catch (Exception e) {
            log.error("Failed to send Discord notification: {}", e.getMessage());
        }
    }

    /**
     * Notify about upcoming payment for service account
     */
    @Async
    public void notifyPaymentReminder(ServiceAccount account, int daysRemaining) {
        String serviceName = account.getMyService().getName();
        String accountLogin = account.getLogin();
        Date paymentDate = account.getPaymentDate();

        log.info("Sending payment reminder for service: {}, days: {}", serviceName, daysRemaining);

        // This is primarily an admin notification for service account payments
        // If there's a specific user associated with managing this account, add user notifications here

        // Discord admin notification
        try {
            discordService.sendPaymentReminderNotification(
                    serviceName,
                    accountLogin,
                    DATE_FORMAT.format(paymentDate),
                    daysRemaining
            );
        } catch (Exception e) {
            log.error("Failed to send Discord payment reminder: {}", e.getMessage());
        }
    }
}
