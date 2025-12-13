package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.config.OneSignalConfig;
import com.mabsplace.mabsplaceback.domain.dtos.onesignal.OneSignalEmailRequest;
import com.mabsplace.mabsplaceback.domain.dtos.onesignal.OneSignalPushRequest;
import com.mabsplace.mabsplaceback.domain.dtos.onesignal.OneSignalSMSRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OneSignalService {

    private final WebClient oneSignalWebClient;
    private final OneSignalConfig oneSignalConfig;

    /**
     * Send email via OneSignal Email API
     */
    @Async
    public void sendEmail(OneSignalEmailRequest request) {
        try {
            log.info("Sending email via OneSignal to: {}", request.getTo());

            Map<String, Object> emailPayload = new HashMap<>();
            emailPayload.put("app_id", oneSignalConfig.getAppId());

            // Create email notification
            Map<String, Object> email = new HashMap<>();
            email.put("template_id", request.getTemplateId());
            email.put("subject", request.getSubject());
            email.put("from_email", request.getFromEmail() != null ?
                request.getFromEmail() : oneSignalConfig.getDefaultFromEmail());

            // Add custom data for template variables
            if (request.getCustomData() != null) {
                email.put("data", request.getCustomData());
            }

            emailPayload.put("email", email);
            emailPayload.put("include_email_tokens", List.of(request.getTo()));

            oneSignalWebClient.post()
                    .uri("/notifications")
                    .bodyValue(emailPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("Email sent successfully via OneSignal: {}", response))
                    .doOnError(error -> log.error("Failed to send email via OneSignal: {}", error.getMessage()))
                    .subscribe();

        } catch (Exception e) {
            log.error("Error sending email via OneSignal", e);
        }
    }

    /**
     * Send push notification via OneSignal Push API
     */
    @Async
    public void sendPushNotification(OneSignalPushRequest request) {
        try {
            log.info("Sending push notification via OneSignal to user: {}", request.getExternalUserId());

            Map<String, Object> pushPayload = new HashMap<>();
            pushPayload.put("app_id", oneSignalConfig.getAppId());

            // Set notification content
            Map<String, String> headings = new HashMap<>();
            headings.put("en", request.getTitle());
            pushPayload.put("headings", headings);

            Map<String, String> contents = new HashMap<>();
            contents.put("en", request.getMessage());
            pushPayload.put("contents", contents);

            // Target specific user by external user ID
            pushPayload.put("include_external_user_ids", List.of(request.getExternalUserId()));

            // Add custom data
            if (request.getData() != null) {
                pushPayload.put("data", request.getData());
            }

            // Add deep link URL
            if (request.getUrl() != null) {
                pushPayload.put("url", request.getUrl());
            }

            oneSignalWebClient.post()
                    .uri("/notifications")
                    .bodyValue(pushPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("Push notification sent successfully via OneSignal: {}", response))
                    .doOnError(error -> log.error("Failed to send push notification via OneSignal: {}", error.getMessage()))
                    .subscribe();

        } catch (Exception e) {
            log.error("Error sending push notification via OneSignal", e);
        }
    }

    /**
     * Send SMS via OneSignal SMS API
     */
    @Async
    public void sendSMS(OneSignalSMSRequest request) {
        try {
            log.info("Sending SMS via OneSignal to: {}", request.getPhoneNumber());

            Map<String, Object> smsPayload = new HashMap<>();
            smsPayload.put("app_id", oneSignalConfig.getAppId());

            // Set SMS content
            Map<String, String> contents = new HashMap<>();
            contents.put("en", request.getMessage());
            smsPayload.put("contents", contents);

            // Target specific phone number
            smsPayload.put("include_phone_numbers", List.of(formatToE164(request.getPhoneNumber())));

            // Add custom data for template variables
            if (request.getCustomData() != null) {
                smsPayload.put("data", request.getCustomData());
            }

            oneSignalWebClient.post()
                    .uri("/notifications")
                    .bodyValue(smsPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("SMS sent successfully via OneSignal: {}", response))
                    .doOnError(error -> log.error("Failed to send SMS via OneSignal: {}", error.getMessage()))
                    .subscribe();

        } catch (Exception e) {
            log.error("Error sending SMS via OneSignal", e);
        }
    }

    /**
     * Create or update user in OneSignal
     */
    public void createOrUpdateUser(Long userId, String email, String phoneNumber, String pushToken) {
        try {
            log.info("Creating/updating user in OneSignal: userId={}", userId);

            Map<String, Object> userPayload = new HashMap<>();
            userPayload.put("app_id", oneSignalConfig.getAppId());
            userPayload.put("external_user_id", userId.toString());

            if (email != null) {
                userPayload.put("email", email);
            }

            if (phoneNumber != null) {
                userPayload.put("phone_number", formatToE164(phoneNumber));
            }

            if (pushToken != null) {
                userPayload.put("identifier", pushToken);
            }

            oneSignalWebClient.post()
                    .uri("/players")
                    .bodyValue(userPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("User created/updated successfully in OneSignal: {}", response))
                    .doOnError(error -> log.error("Failed to create/update user in OneSignal: {}", error.getMessage()))
                    .subscribe();

        } catch (Exception e) {
            log.error("Error creating/updating user in OneSignal", e);
        }
    }

    /**
     * Tag user in OneSignal for segmentation
     */
    public void tagUser(String userId, Map<String, String> tags) {
        try {
            log.info("Tagging user in OneSignal: userId={}, tags={}", userId, tags);

            Map<String, Object> tagPayload = new HashMap<>();
            tagPayload.put("tags", tags);

            oneSignalWebClient.put()
                    .uri("/players/{userId}", userId)
                    .bodyValue(tagPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("User tagged successfully in OneSignal: {}", response))
                    .doOnError(error -> log.error("Failed to tag user in OneSignal: {}", error.getMessage()))
                    .subscribe();

        } catch (Exception e) {
            log.error("Error tagging user in OneSignal", e);
        }
    }

    /**
     * Format phone number to E.164 format (+1234567890)
     */
    private String formatToE164(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return phoneNumber;
        }

        // If already in E.164 format, return as-is
        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        }

        // Add country code - assuming Cameroon (+237) as default
        // Adjust this logic based on your user base
        return "+237" + phoneNumber.replaceAll("[^0-9]", "");
    }

    /**
     * Send multi-channel notification (email + push + SMS)
     */
    @Async
    public void sendMultiChannel(Long userId, String emailTemplateId,
                                 String pushTemplateId, String smsTemplateId,
                                 Map<String, String> data) {
        try {
            log.info("Sending multi-channel notification to user: {}", userId);

            // Send email
            if (emailTemplateId != null) {
                OneSignalEmailRequest emailRequest = OneSignalEmailRequest.builder()
                        .to(data.get("email"))
                        .subject(data.get("subject"))
                        .templateId(emailTemplateId)
                        .customData(data)
                        .build();
                sendEmail(emailRequest);
            }

            // Send push notification
            if (pushTemplateId != null) {
                OneSignalPushRequest pushRequest = OneSignalPushRequest.builder()
                        .externalUserId(userId.toString())
                        .title(data.get("title"))
                        .message(data.get("message"))
                        .templateId(pushTemplateId)
                        .data(new HashMap<>(data))
                        .build();
                sendPushNotification(pushRequest);
            }

            // Send SMS
            if (smsTemplateId != null && data.get("phone") != null) {
                OneSignalSMSRequest smsRequest = OneSignalSMSRequest.builder()
                        .phoneNumber(data.get("phone"))
                        .message(data.get("message"))
                        .templateId(smsTemplateId)
                        .customData(data)
                        .build();
                sendSMS(smsRequest);
            }

        } catch (Exception e) {
            log.error("Error sending multi-channel notification", e);
        }
    }
}
