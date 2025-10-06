package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.services.DiscordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "Test endpoints for development and debugging")
public class TestController {

    private final DiscordService discordService;

    public TestController(DiscordService discordService) {
        this.discordService = discordService;
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
}
