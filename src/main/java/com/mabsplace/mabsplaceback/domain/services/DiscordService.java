package com.mabsplace.mabsplaceback.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DiscordService {

    private static final Logger logger = LoggerFactory.getLogger(DiscordService.class);

    // Rate limiting: ~50 requests per minute = 1.2 seconds between requests
    private static final long MIN_REQUEST_INTERVAL_MS = 1200;
    private static final int MAX_RETRIES = 3;

    // Track last request time per webhook URL to enforce rate limiting
    private final ConcurrentHashMap<String, Long> lastRequestTime = new ConcurrentHashMap<>();

    @Value("${discord.webhook.payment-reminders:}")
    private String paymentRemindersWebhook;

    @Value("${discord.webhook.subscription-renewals:}")
    private String subscriptionRenewalsWebhook;

    @Value("${discord.webhook.profile-security-audit:}")
    private String profileSecurityAuditWebhook;

    private final RestTemplate restTemplate;

    public DiscordService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Send a Discord webhook request with rate limiting and retry logic
     */
    private void sendWithRateLimitAndRetry(String webhookUrl, Map<String, Object> payload) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                // Enforce rate limiting
                enforceRateLimit(webhookUrl);

                // Send request
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                restTemplate.postForObject(webhookUrl, request, String.class);

                // Success - update last request time
                lastRequestTime.put(webhookUrl, System.currentTimeMillis());
                return;

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) {
                    // Rate limited by Discord
                    int retryAfterMs = parseRetryAfter(e.getResponseBodyAsString());
                    logger.warn("Discord rate limited (attempt {}/{}). Waiting {}ms before retry",
                            attempt, MAX_RETRIES, retryAfterMs);

                    if (attempt < MAX_RETRIES) {
                        sleep(retryAfterMs);
                    } else {
                        logger.error("Discord notification failed after {} retries due to rate limiting", MAX_RETRIES);
                    }
                } else {
                    // Other HTTP error - log and give up
                    logger.error("Discord notification failed with HTTP {}: {}",
                            e.getStatusCode(), e.getMessage());
                    return;
                }
            } catch (Exception e) {
                // Unexpected error
                logger.error("Unexpected error sending Discord notification (attempt {}/{}): {}",
                        attempt, MAX_RETRIES, e.getMessage());

                if (attempt < MAX_RETRIES) {
                    // Exponential backoff: 2s, 4s, 8s
                    int backoffMs = (int) (Math.pow(2, attempt) * 1000);
                    sleep(backoffMs);
                } else {
                    logger.error("Discord notification failed after {} retries", MAX_RETRIES);
                }
            }
        }
    }

    /**
     * Enforce minimum delay between requests to same webhook
     */
    private void enforceRateLimit(String webhookUrl) {
        Long lastTime = lastRequestTime.get(webhookUrl);
        if (lastTime != null) {
            long timeSinceLastRequest = System.currentTimeMillis() - lastTime;
            if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
                long sleepTime = MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest;
                sleep(sleepTime);
            }
        }
    }

    /**
     * Parse retry_after from Discord 429 response
     */
    private int parseRetryAfter(String responseBody) {
        try {
            // Discord returns: {"message": "You are being rate limited.", "retry_after": 0.377, "global": false}
            // Parse the retry_after value (in seconds) and convert to milliseconds
            String retryAfterStr = responseBody.split("\"retry_after\":\\s*")[1].split("[,}]")[0].trim();
            double retryAfterSeconds = Double.parseDouble(retryAfterStr);
            return (int) Math.ceil(retryAfterSeconds * 1000) + 500; // Add 500ms buffer
        } catch (Exception e) {
            logger.warn("Could not parse retry_after from Discord response, using default 60s");
            return 60000; // Default to 60 seconds
        }
    }

    /**
     * Sleep helper with exception handling
     */
    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Sleep interrupted: {}", e.getMessage());
        }
    }

    /**
     * Build a standard Discord embed payload
     */
    private Map<String, Object> buildDiscordPayload(String title, String description, Integer color, String footerText) {
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", title);
        embed.put("description", description);
        embed.put("color", color != null ? color : 3447003);
        embed.put("timestamp", Instant.now().toString());

        Map<String, Object> footer = new HashMap<>();
        footer.put("text", footerText != null ? footerText : "MabsPlace");
        embed.put("footer", footer);

        Map<String, Object> payload = new HashMap<>();
        payload.put("embeds", List.of(embed));

        return payload;
    }

    @Async
    public void sendPaymentReminderNotification(String serviceName, String accountLogin, String paymentDate, int daysUntilPayment) {
        if (paymentRemindersWebhook == null || paymentRemindersWebhook.isEmpty()) {
            logger.warn("Discord webhook URL not configured for payment reminders");
            return;
        }

        logger.info("Sending Discord notification for payment reminder: {} days until payment", daysUntilPayment);

        String title = daysUntilPayment <= 3 ? "🚨 Urgent: Subscription Payment Due Soon" : "⏰ Upcoming Subscription Payment";
        String description = String.format(
                "**Service:** %s\n**Account:** %s\n**Payment Date:** %s\n**Days Remaining:** %d",
                serviceName, accountLogin, paymentDate, daysUntilPayment
        );
        Integer color = daysUntilPayment <= 3 ? 15158332 : 3447003; // Red for urgent, Blue for normal

        Map<String, Object> payload = buildDiscordPayload(title, description, color, "MabsPlace Payment Reminder");
        sendWithRateLimitAndRetry(paymentRemindersWebhook, payload);

        logger.info("Discord notification sent successfully for payment reminder");
    }

    @Async
    public void sendCustomNotification(String webhookUrl, String title, String description, Integer color) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            logger.warn("Discord webhook URL not provided");
            return;
        }

        logger.info("Sending custom Discord notification: {}", title);

        Map<String, Object> payload = buildDiscordPayload(title, description, color, "MabsPlace");
        sendWithRateLimitAndRetry(webhookUrl, payload);

        logger.info("Custom Discord notification sent successfully");
    }

    @Async
    public void sendSubscriptionRenewedNotification(String username, String serviceName, String newEndDate) {
        if (subscriptionRenewalsWebhook == null || subscriptionRenewalsWebhook.isEmpty()) {
            logger.warn("Discord webhook URL not configured for subscription renewals");
            return;
        }

        logger.info("Sending Discord notification for subscription renewal: {} - {}", username, serviceName);

        String description = String.format(
                "**User:** %s\n**Service:** %s\n**New End Date:** %s",
                username, serviceName, newEndDate
        );

        Map<String, Object> payload = buildDiscordPayload("✅ Subscription Renewed", description, 5763719, "MabsPlace Subscription Renewals");
        sendWithRateLimitAndRetry(subscriptionRenewalsWebhook, payload);

        logger.info("Discord notification sent successfully for subscription renewal");
    }

    @Async
    public void sendSubscriptionRenewalFailedNotification(String username, String serviceName, int attemptNumber) {
        if (subscriptionRenewalsWebhook == null || subscriptionRenewalsWebhook.isEmpty()) {
            logger.warn("Discord webhook URL not configured for subscription renewals");
            return;
        }

        logger.info("Sending Discord notification for failed renewal: {} - {}", username, serviceName);

        String description = String.format(
                "**User:** %s\n**Service:** %s\n**Attempt:** %d/4\n**Status:** Will retry in 24 hours",
                username, serviceName, attemptNumber
        );

        Map<String, Object> payload = buildDiscordPayload("⚠️ Subscription Renewal Failed", description, 16776960, "MabsPlace Subscription Renewals");
        sendWithRateLimitAndRetry(subscriptionRenewalsWebhook, payload);

        logger.info("Discord notification sent successfully for failed renewal");
    }

    @Async
    public void sendSubscriptionExpiredNotification(String username, String serviceName, String accountLogin, String profileName) {
        if (subscriptionRenewalsWebhook == null || subscriptionRenewalsWebhook.isEmpty()) {
            logger.warn("Discord webhook URL not configured for subscription renewals");
            return;
        }

        logger.info("Sending Discord notification for expired subscription: {} - {}", username, serviceName);

        String description = String.format(
                "**User:** %s\n**Service:** %s\n**Account:** %s\n**Profile:** %s\n\n⚠️ **Action Required:** Change account password/PIN",
                username, serviceName, accountLogin, profileName
        );

        Map<String, Object> payload = buildDiscordPayload("❌ Subscription Expired", description, 15158332, "MabsPlace Subscription Renewals");
        sendWithRateLimitAndRetry(subscriptionRenewalsWebhook, payload);

        logger.info("Discord notification sent successfully for expired subscription");
    }

    @Async
    public void sendSubscriptionExpiringNotification(String username, String serviceName, String expirationDate, String accountLogin, String profileName) {
        if (subscriptionRenewalsWebhook == null || subscriptionRenewalsWebhook.isEmpty()) {
            logger.warn("Discord webhook URL not configured for subscription renewals");
            return;
        }

        logger.info("Sending Discord notification for expiring subscription: {} - {}", username, serviceName);

        String description = String.format(
                "**User:** %s\n**Service:** %s\n**Expiration Date:** %s\n**Account:** %s\n**Profile:** %s",
                username, serviceName, expirationDate, accountLogin, profileName
        );

        Map<String, Object> payload = buildDiscordPayload("⏰ Subscription Expiring Soon", description, 3447003, "MabsPlace Subscription Renewals");
        sendWithRateLimitAndRetry(subscriptionRenewalsWebhook, payload);

        logger.info("Discord notification sent successfully for expiring subscription");
    }

    @Async
    public void sendProfileSecurityAuditAlert(Long subscriptionId, String username, String serviceName,
                                               String profileName, String expiryDate) {
        if (profileSecurityAuditWebhook == null || profileSecurityAuditWebhook.isEmpty()) {
            logger.warn("Discord webhook URL not configured for profile security audit");
            return;
        }

        logger.info("Sending Discord security audit alert for subscription: {} - {} - {}",
                    subscriptionId, username, serviceName);

        String description = String.format(
                "**⚠️ User may still have access to expired subscription**\n\n" +
                "**Subscription ID:** %d\n" +
                "**User:** %s\n" +
                "**Service:** %s\n" +
                "**Profile Name:** %s\n" +
                "**Expired On:** %s\n\n" +
                "**Action Required:**\n" +
                "• Change profile name to 'empty' or 'empty2'\n" +
                "• Reset profile PIN\n" +
                "• Update service account password if necessary",
                subscriptionId, username, serviceName, profileName, expiryDate
        );

        Map<String, Object> payload = buildDiscordPayload("🔒 Security Alert: Profile Not Reset", description, 15158332, "MabsPlace Profile Security Audit");
        sendWithRateLimitAndRetry(profileSecurityAuditWebhook, payload);

        logger.info("Discord security audit alert sent successfully for subscription: {}", subscriptionId);
    }

    @Async
    public void sendPaymentConfirmationNotification(String username, String serviceName, String amount, String currency) {
        if (paymentRemindersWebhook == null || paymentRemindersWebhook.isEmpty()) {
            logger.warn("Discord webhook URL not configured for payment reminders");
            return;
        }

        logger.info("Sending Discord notification for payment confirmation: {} - {}", username, serviceName);

        String description = String.format(
                "**User:** %s\n**Service:** %s\n**Amount:** %s %s\n**Status:** Successfully Processed",
                username, serviceName, currency, amount
        );

        Map<String, Object> payload = buildDiscordPayload("💰 Payment Confirmed", description, 5763719, "MabsPlace Payment Confirmation");
        sendWithRateLimitAndRetry(paymentRemindersWebhook, payload);

        logger.info("Discord notification sent successfully for payment confirmation");
    }

    /**
     * Send alert to Discord about subscription created without profile
     */
    @Async
    public void sendMissingProfileAlert(Long subscriptionId, String username, String serviceName, Long userId) {
        if (profileSecurityAuditWebhook == null || profileSecurityAuditWebhook.isEmpty()) {
            logger.warn("Missing profile alert webhook not configured - skipping Discord notification");
            return;
        }

        logger.info("Sending missing profile alert for subscription ID: {}", subscriptionId);

        Map<String, Object> embed = new HashMap<>();
        embed.put("title", "🚨 Subscription Created Without Profile");
        embed.put("description", String.format(
            "A new subscription was purchased but no profile was available for assignment.\n\n" +
            "**Subscription ID:** %d\n" +
            "**User:** %s (ID: %d)\n" +
            "**Service:** %s\n\n" +
            "⚠️ **Action Required:** Assign a profile to this subscription using the admin panel.",
            subscriptionId, username, userId, serviceName
        ));
        embed.put("color", 15158332); // Red color
        embed.put("timestamp", Instant.now().toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("embeds", List.of(embed));

        sendWithRateLimitAndRetry(profileSecurityAuditWebhook, payload);
    }
}
