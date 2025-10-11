package com.mabsplace.mabsplaceback.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiscordService {

    private static final Logger logger = LoggerFactory.getLogger(DiscordService.class);

    @Value("${discord.webhook.payment-reminders:}")
    private String paymentRemindersWebhook;

    @Value("${discord.webhook.subscription-renewals:}")
    private String subscriptionRenewalsWebhook;

    private final RestTemplate restTemplate;

    public DiscordService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    public void sendPaymentReminderNotification(String serviceName, String accountLogin, String paymentDate, int daysUntilPayment) {
        if (paymentRemindersWebhook == null || paymentRemindersWebhook.isEmpty()) {
            logger.warn("Discord webhook URL not configured for payment reminders");
            return;
        }

        try {
            logger.info("Sending Discord notification for payment reminder: {} days until payment", daysUntilPayment);

            Map<String, Object> embed = new HashMap<>();
            embed.put("title", daysUntilPayment <= 3 ? "🚨 Urgent: Subscription Payment Due Soon" : "⏰ Upcoming Subscription Payment");
            embed.put("description", String.format(
                    "**Service:** %s\n**Account:** %s\n**Payment Date:** %s\n**Days Remaining:** %d",
                    serviceName, accountLogin, paymentDate, daysUntilPayment
            ));
            embed.put("color", daysUntilPayment <= 3 ? 15158332 : 3447003); // Red for urgent, Blue for normal
            embed.put("timestamp", Instant.now().toString());

            Map<String, Object> footer = new HashMap<>();
            footer.put("text", "MabsPlace Payment Reminder");
            embed.put("footer", footer);

            Map<String, Object> payload = new HashMap<>();
            payload.put("embeds", List.of(embed));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(paymentRemindersWebhook, request, String.class);

            logger.info("Discord notification sent successfully for payment reminder");
        } catch (Exception e) {
            logger.error("Failed to send Discord notification: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendCustomNotification(String webhookUrl, String title, String description, Integer color) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            logger.warn("Discord webhook URL not provided");
            return;
        }

        try {
            logger.info("Sending custom Discord notification: {}", title);

            Map<String, Object> embed = new HashMap<>();
            embed.put("title", title);
            embed.put("description", description);
            embed.put("color", color != null ? color : 3447003); // Default blue
            embed.put("timestamp", Instant.now().toString());

            Map<String, Object> footer = new HashMap<>();
            footer.put("text", "MabsPlace");
            embed.put("footer", footer);

            Map<String, Object> payload = new HashMap<>();
            payload.put("embeds", List.of(embed));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(webhookUrl, request, String.class);

            logger.info("Custom Discord notification sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send custom Discord notification: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendSubscriptionRenewedNotification(String username, String serviceName, String newEndDate) {
        if (subscriptionRenewalsWebhook == null || subscriptionRenewalsWebhook.isEmpty()) {
            logger.warn("Discord webhook URL not configured for subscription renewals");
            return;
        }

        try {
            logger.info("Sending Discord notification for subscription renewal: {} - {}", username, serviceName);

            Map<String, Object> embed = new HashMap<>();
            embed.put("title", "✅ Subscription Renewed");
            embed.put("description", String.format(
                    "**User:** %s\n**Service:** %s\n**New End Date:** %s",
                    username, serviceName, newEndDate
            ));
            embed.put("color", 5763719); // Green color
            embed.put("timestamp", Instant.now().toString());

            Map<String, Object> footer = new HashMap<>();
            footer.put("text", "MabsPlace Subscription Renewals");
            embed.put("footer", footer);

            Map<String, Object> payload = new HashMap<>();
            payload.put("embeds", List.of(embed));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(subscriptionRenewalsWebhook, request, String.class);

            logger.info("Discord notification sent successfully for subscription renewal");
        } catch (Exception e) {
            logger.error("Failed to send Discord notification for renewal: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendSubscriptionRenewalFailedNotification(String username, String serviceName, int attemptNumber) {
        if (subscriptionRenewalsWebhook == null || subscriptionRenewalsWebhook.isEmpty()) {
            logger.warn("Discord webhook URL not configured for subscription renewals");
            return;
        }

        try {
            logger.info("Sending Discord notification for failed renewal: {} - {}", username, serviceName);

            Map<String, Object> embed = new HashMap<>();
            embed.put("title", "⚠️ Subscription Renewal Failed");
            embed.put("description", String.format(
                    "**User:** %s\n**Service:** %s\n**Attempt:** %d/4\n**Status:** Will retry in 24 hours",
                    username, serviceName, attemptNumber
            ));
            embed.put("color", 16776960); // Yellow/Orange color
            embed.put("timestamp", Instant.now().toString());

            Map<String, Object> footer = new HashMap<>();
            footer.put("text", "MabsPlace Subscription Renewals");
            embed.put("footer", footer);

            Map<String, Object> payload = new HashMap<>();
            payload.put("embeds", List.of(embed));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(subscriptionRenewalsWebhook, request, String.class);

            logger.info("Discord notification sent successfully for failed renewal");
        } catch (Exception e) {
            logger.error("Failed to send Discord notification for failed renewal: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendSubscriptionExpiredNotification(String username, String serviceName, String accountLogin, String profileName) {
        if (subscriptionRenewalsWebhook == null || subscriptionRenewalsWebhook.isEmpty()) {
            logger.warn("Discord webhook URL not configured for subscription renewals");
            return;
        }

        try {
            logger.info("Sending Discord notification for expired subscription: {} - {}", username, serviceName);

            Map<String, Object> embed = new HashMap<>();
            embed.put("title", "❌ Subscription Expired");
            embed.put("description", String.format(
                    "**User:** %s\n**Service:** %s\n**Account:** %s\n**Profile:** %s\n\n⚠️ **Action Required:** Change account password/PIN",
                    username, serviceName, accountLogin, profileName
            ));
            embed.put("color", 15158332); // Red color
            embed.put("timestamp", Instant.now().toString());

            Map<String, Object> footer = new HashMap<>();
            footer.put("text", "MabsPlace Subscription Renewals");
            embed.put("footer", footer);

            Map<String, Object> payload = new HashMap<>();
            payload.put("embeds", List.of(embed));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(subscriptionRenewalsWebhook, request, String.class);

            logger.info("Discord notification sent successfully for expired subscription");
        } catch (Exception e) {
            logger.error("Failed to send Discord notification for expired subscription: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendSubscriptionExpiringNotification(String username, String serviceName, String expirationDate, String accountLogin, String profileName) {
        if (subscriptionRenewalsWebhook == null || subscriptionRenewalsWebhook.isEmpty()) {
            logger.warn("Discord webhook URL not configured for subscription renewals");
            return;
        }

        try {
            logger.info("Sending Discord notification for expiring subscription: {} - {}", username, serviceName);

            Map<String, Object> embed = new HashMap<>();
            embed.put("title", "⏰ Subscription Expiring Soon");
            embed.put("description", String.format(
                    "**User:** %s\n**Service:** %s\n**Expiration Date:** %s\n**Account:** %s\n**Profile:** %s",
                    username, serviceName, expirationDate, accountLogin, profileName
            ));
            embed.put("color", 3447003); // Blue color
            embed.put("timestamp", Instant.now().toString());

            Map<String, Object> footer = new HashMap<>();
            footer.put("text", "MabsPlace Subscription Renewals");
            embed.put("footer", footer);

            Map<String, Object> payload = new HashMap<>();
            payload.put("embeds", List.of(embed));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(subscriptionRenewalsWebhook, request, String.class);

            logger.info("Discord notification sent successfully for expiring subscription");
        } catch (Exception e) {
            logger.error("Failed to send Discord notification for expiring subscription: {}", e.getMessage(), e);
        }
    }
}
