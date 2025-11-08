package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.services.DiscordService;
import com.mabsplace.mabsplaceback.domain.services.WhatsAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "Test endpoints for development and debugging")
public class TestController {

    private final DiscordService discordService;
    private final WhatsAppService whatsAppService;

    public TestController(DiscordService discordService, WhatsAppService whatsAppService) {
        this.discordService = discordService;
        this.whatsAppService = whatsAppService;
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
}
