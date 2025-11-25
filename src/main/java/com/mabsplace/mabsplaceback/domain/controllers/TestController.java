package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import com.mabsplace.mabsplaceback.domain.repositories.SubscriptionRepository;
import com.mabsplace.mabsplaceback.domain.services.DiscordService;
import com.mabsplace.mabsplaceback.domain.services.SubscriptionService;
import com.mabsplace.mabsplaceback.domain.services.WhatsAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "Test endpoints for development and debugging")
public class TestController {

    private final DiscordService discordService;
    private final WhatsAppService whatsAppService;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

    public TestController(DiscordService discordService, WhatsAppService whatsAppService, SubscriptionRepository subscriptionRepository, SubscriptionService subscriptionService) {
        this.discordService = discordService;
        this.whatsAppService = whatsAppService;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/discord/payment-reminder")
    @Operation(summary = "Test Discord payment reminder notification")
    public ResponseEntity<String> testDiscordPaymentReminder() {
        discordService.sendPaymentReminderNotification(
                "Netflix Premium",
                "john.doe@example.com",
                "2025-10-13",
                7
        );
        return ResponseEntity.ok("Test Discord notification sent! Check your Discord channel.");
    }

    @PostMapping("/discord/urgent-reminder")
    @Operation(summary = "Test Discord urgent payment reminder notification")
    public ResponseEntity<String> testDiscordUrgentReminder() {
        discordService.sendPaymentReminderNotification(
                "Spotify Family",
                "team@mabsplace.com",
                "2025-10-09",
                3
        );
        return ResponseEntity.ok("Test urgent Discord notification sent! Check your Discord channel.");
    }

    @PostMapping("/whatsapp/subscription-renewed")
    @Operation(summary = "Test WhatsApp subscription renewed notification")
    public ResponseEntity<String> testWhatsAppSubscriptionRenewed(
            @RequestParam(defaultValue = "237695902832") String phoneNumber) {
        whatsAppService.sendSubscriptionRenewedNotification(
                phoneNumber,
                "John Doe",
                "Netflix Premium",
                "2025-12-15"
        );
        return ResponseEntity.ok("WhatsApp subscription renewed notification sent to: " + phoneNumber);
    }

    @PostMapping("/whatsapp/subscription-renewal-failed")
    @Operation(summary = "Test WhatsApp subscription renewal failed notification")
    public ResponseEntity<String> testWhatsAppSubscriptionRenewalFailed(
            @RequestParam(defaultValue = "237695902832") String phoneNumber) {
        whatsAppService.sendSubscriptionRenewalFailedNotification(
                phoneNumber,
                "Jane Smith",
                "Spotify Family",
                2
        );
        return ResponseEntity.ok("WhatsApp subscription renewal failed notification sent to: " + phoneNumber);
    }

    @PostMapping("/whatsapp/subscription-expired")
    @Operation(summary = "Test WhatsApp subscription expired notification")
    public ResponseEntity<String> testWhatsAppSubscriptionExpired(
            @RequestParam(defaultValue = "237695902832") String phoneNumber) {
        whatsAppService.sendSubscriptionExpiredNotification(
                phoneNumber,
                "Mike Johnson",
                "Disney+",
                "Profile 1"
        );
        return ResponseEntity.ok("WhatsApp subscription expired notification sent to: " + phoneNumber);
    }

    @PostMapping("/whatsapp/subscription-expiring-soon")
    @Operation(summary = "Test WhatsApp subscription expiring soon notification")
    public ResponseEntity<String> testWhatsAppSubscriptionExpiringSoon(
            @RequestParam(defaultValue = "237695902832") String phoneNumber) {
        whatsAppService.sendSubscriptionExpiringNotification(
                phoneNumber,
                "Sarah Williams",
                "Amazon Prime",
                "2025-11-15",
                7
        );
        return ResponseEntity.ok("WhatsApp subscription expiring soon notification sent to: " + phoneNumber);
    }

    @GetMapping("/subscriptions/renewal-diagnostic")
    @Operation(summary = "Diagnostic endpoint to check which subscriptions would be processed by renewal cron job")
    public ResponseEntity<Map<String, Object>> subscriptionRenewalDiagnostic() {
        Date today = new Date();

        // Get all subscriptions
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();

        // Get subscriptions that meet renewal criteria (same as cron job)
        List<Subscription> subscriptionsToRenew = subscriptionRepository
                .findByStatusAndEndDateBeforeAndAutoRenewTrue(
                        SubscriptionStatus.ACTIVE,
                        today
                );

        // Get subscriptions with autoRenew=false that are expired
        List<Subscription> expiredNoAutoRenew = subscriptionRepository
                .findByEndDateBeforeAndStatusNotAndAutoRenewFalse(today, SubscriptionStatus.EXPIRED);

        // Categorize all subscriptions
        Map<String, List<Map<String, Object>>> categorized = new HashMap<>();
        categorized.put("willBeRenewed", new ArrayList<>());
        categorized.put("willBeExpired", new ArrayList<>());
        categorized.put("alreadyExpired", new ArrayList<>());
        categorized.put("activeFutureEndDate", new ArrayList<>());
        categorized.put("activeButNoAutoRenew", new ArrayList<>());

        for (Subscription sub : allSubscriptions) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", sub.getId());
            info.put("username", sub.getUser().getUsername());
            info.put("service", sub.getService().getName());
            info.put("endDate", sub.getEndDate());
            info.put("status", sub.getStatus());
            info.put("autoRenew", sub.isAutoRenew());
            info.put("renewalAttempts", sub.getRenewalAttempts());

            if (sub.getStatus() == SubscriptionStatus.ACTIVE &&
                sub.getEndDate().before(today) &&
                sub.isAutoRenew()) {
                categorized.get("willBeRenewed").add(info);
            } else if (sub.getStatus() != SubscriptionStatus.EXPIRED &&
                       sub.getEndDate().before(today) &&
                       !sub.isAutoRenew()) {
                categorized.get("willBeExpired").add(info);
            } else if (sub.getStatus() == SubscriptionStatus.EXPIRED) {
                categorized.get("alreadyExpired").add(info);
            } else if (sub.getStatus() == SubscriptionStatus.ACTIVE &&
                       sub.getEndDate().after(today) &&
                       !sub.isAutoRenew()) {
                categorized.get("activeButNoAutoRenew").add(info);
            } else if (sub.getStatus() == SubscriptionStatus.ACTIVE &&
                       sub.getEndDate().after(today)) {
                categorized.get("activeFutureEndDate").add(info);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("currentDate", today);
        response.put("totalSubscriptions", allSubscriptions.size());
        response.put("willBeProcessedByRenewalCron", subscriptionsToRenew.size());
        response.put("willBeProcessedByExpireCron", expiredNoAutoRenew.size());
        response.put("breakdown", categorized);
        response.put("summary", Map.of(
            "willBeRenewed", categorized.get("willBeRenewed").size(),
            "willBeExpiredOnly", categorized.get("willBeExpired").size(),
            "alreadyExpired", categorized.get("alreadyExpired").size(),
            "activeFutureEndDate", categorized.get("activeFutureEndDate").size(),
            "activeButNoAutoRenew", categorized.get("activeButNoAutoRenew").size()
        ));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cron/process-renewals")
    @Operation(summary = "Manually trigger the subscription renewal cron job (autoRenew=true)")
    public ResponseEntity<Map<String, Object>> triggerRenewalCronJob() {
        try {
            Date today = new Date();
            List<Subscription> subscriptionsToRenew = subscriptionRepository
                    .findByStatusAndEndDateBeforeAndAutoRenewTrue(
                            SubscriptionStatus.ACTIVE,
                            today
                    );

            subscriptionService.processSubscriptionRenewals();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Renewal cron job triggered successfully");
            response.put("subscriptionsProcessed", subscriptionsToRenew.size());
            response.put("timestamp", new Date());

            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error processing renewals: " + e.getMessage());
            errorResponse.put("timestamp", new Date());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/cron/expire-subscriptions")
    @Operation(summary = "Manually trigger the subscription expiration cron job (autoRenew=false)")
    public ResponseEntity<Map<String, Object>> triggerExpirationCronJob() {
        try {
            Date today = new Date();
            List<Subscription> subscriptionsToExpire = subscriptionRepository
                    .findByEndDateBeforeAndStatusNotAndAutoRenewFalse(today, SubscriptionStatus.EXPIRED);

            subscriptionService.expireSubscriptions();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expiration cron job triggered successfully");
            response.put("subscriptionsProcessed", subscriptionsToExpire.size());
            response.put("timestamp", new Date());

            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error expiring subscriptions: " + e.getMessage());
            errorResponse.put("timestamp", new Date());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/cron/notify-expiring")
    @Operation(summary = "Manually trigger the expiring subscription notification cron job")
    public ResponseEntity<Map<String, Object>> triggerExpiringNotificationCronJob() {
        try {
            subscriptionService.notifyExpiringSubscriptions();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expiring notification cron job triggered successfully");
            response.put("timestamp", new Date());

            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error notifying expiring subscriptions: " + e.getMessage());
            errorResponse.put("timestamp", new Date());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/cron/audit-profiles")
    @Operation(summary = "Manually trigger the profile security audit cron job")
    public ResponseEntity<Map<String, Object>> triggerProfileAuditCronJob() {
        try {
            subscriptionService.auditExpiredSubscriptionProfiles();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile security audit cron job triggered successfully");
            response.put("timestamp", new Date());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error auditing profiles: " + e.getMessage());
            errorResponse.put("timestamp", new Date());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
