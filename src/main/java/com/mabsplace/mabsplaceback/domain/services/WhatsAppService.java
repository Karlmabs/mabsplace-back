package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.whatsapp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${whatsapp.api.url}")
    private String whatsappApiUrl;

    @Value("${whatsapp.api.token}")
    private String apiToken;

    @Value("${whatsapp.phone.number.id}")
    private String phoneNumberId;

    @Value("${whatsapp.templates.subscription-renewed}")
    private String subscriptionRenewedTemplate;

    @Value("${whatsapp.templates.subscription-renewal-failed}")
    private String subscriptionRenewalFailedTemplate;

    @Value("${whatsapp.templates.subscription-expired}")
    private String subscriptionExpiredTemplate;

    @Value("${whatsapp.templates.subscription-expiring-soon}")
    private String subscriptionExpiringSoonTemplate;

    private final RestTemplate restTemplate;

    public WhatsAppService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Generic method to send a WhatsApp template message
     */
    private void sendTemplateMessage(String to, String templateName, String languageCode,
                                     List<WhatsAppComponent> components) {
        if (whatsappApiUrl == null || whatsappApiUrl.isEmpty()) {
            logger.warn("WhatsApp API URL not configured");
            return;
        }

        if (apiToken == null || apiToken.isEmpty()) {
            logger.warn("WhatsApp API token not configured");
            return;
        }

        try {
            logger.info("Sending WhatsApp message to: {} using template: {}", to, templateName);

            String url = String.format("%s/%s/messages", whatsappApiUrl, phoneNumberId);

            WhatsAppTemplateRequest template = WhatsAppTemplateRequest.builder()
                    .name(templateName)
                    .language(WhatsAppLanguage.builder().code(languageCode).build())
                    .components(components)
                    .build();

            WhatsAppMessageRequest request = WhatsAppMessageRequest.builder()
                    .messagingProduct("whatsapp")
                    .to(to)
                    .type("template")
                    .template(template)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiToken);

            HttpEntity<WhatsAppMessageRequest> entity = new HttpEntity<>(request, headers);

            WhatsAppResponse response = restTemplate.postForObject(url, entity, WhatsAppResponse.class);

            if (response != null && response.getMessages() != null && response.getMessages().length > 0) {
                logger.info("WhatsApp message sent successfully. Message ID: {}",
                           response.getMessages()[0].getId());
            } else {
                logger.warn("WhatsApp message sent but no message ID returned");
            }

        } catch (Exception e) {
            logger.error("Failed to send WhatsApp message to: {}. Error: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Template 1: Subscription Renewed
     * Variables: {{1}} username, {{2}} service_name, {{3}} new_end_date
     */
    @Async
    public void sendSubscriptionRenewedNotification(String phoneNumber, String username,
                                                     String serviceName, String newEndDate) {
        logger.info("Sending subscription renewed notification to: {}", phoneNumber);

        List<WhatsAppParameter> bodyParams = new ArrayList<>();
        bodyParams.add(WhatsAppParameter.builder().type("text").text(username).build());
        bodyParams.add(WhatsAppParameter.builder().type("text").text(serviceName).build());
        bodyParams.add(WhatsAppParameter.builder().type("text").text(newEndDate).build());

        List<WhatsAppComponent> components = new ArrayList<>();
        components.add(WhatsAppComponent.builder()
                .type("body")
                .parameters(bodyParams)
                .build());

        sendTemplateMessage(phoneNumber, subscriptionRenewedTemplate, "fr", components);
    }

    /**
     * Template 2: Subscription Renewal Failed
     * Variables: {{1}} username, {{2}} service_name, {{3}} attempt_number
     */
    @Async
    public void sendSubscriptionRenewalFailedNotification(String phoneNumber, String username,
                                                          String serviceName, int attemptNumber) {
        logger.info("Sending subscription renewal failed notification to: {}", phoneNumber);

        List<WhatsAppParameter> bodyParams = new ArrayList<>();
        bodyParams.add(WhatsAppParameter.builder().type("text").text(username).build());
        bodyParams.add(WhatsAppParameter.builder().type("text").text(serviceName).build());
        bodyParams.add(WhatsAppParameter.builder().type("text").text(String.valueOf(attemptNumber)).build());

        List<WhatsAppComponent> components = new ArrayList<>();
        components.add(WhatsAppComponent.builder()
                .type("body")
                .parameters(bodyParams)
                .build());

        sendTemplateMessage(phoneNumber, subscriptionRenewalFailedTemplate, "fr", components);
    }

    /**
     * Template 3: Subscription Expired
     * Variables: {{1}} username, {{2}} service_name, {{3}} profile_name
     */
    @Async
    public void sendSubscriptionExpiredNotification(String phoneNumber, String username,
                                                    String serviceName, String profileName) {
        logger.info("Sending subscription expired notification to: {}", phoneNumber);

        List<WhatsAppParameter> bodyParams = new ArrayList<>();
        bodyParams.add(WhatsAppParameter.builder().type("text").text(username).build());
        bodyParams.add(WhatsAppParameter.builder().type("text").text(serviceName).build());
        bodyParams.add(WhatsAppParameter.builder().type("text").text(profileName).build());

        List<WhatsAppComponent> components = new ArrayList<>();
        components.add(WhatsAppComponent.builder()
                .type("body")
                .parameters(bodyParams)
                .build());

        sendTemplateMessage(phoneNumber, subscriptionExpiredTemplate, "fr", components);
    }

    /**
     * Template 4: Subscription Expiring Soon
     * Variables: {{1}} username, {{2}} service_name, {{3}} expiration_date, {{4}} days_remaining
     */
    @Async
    public void sendSubscriptionExpiringNotification(String phoneNumber, String username,
                                                     String serviceName, String expirationDate,
                                                     int daysRemaining) {
        logger.info("Sending subscription expiring notification to: {}", phoneNumber);

        List<WhatsAppParameter> bodyParams = new ArrayList<>();
        bodyParams.add(WhatsAppParameter.builder().type("text").text(username).build());
        bodyParams.add(WhatsAppParameter.builder().type("text").text(serviceName).build());
        bodyParams.add(WhatsAppParameter.builder().type("text").text(expirationDate).build());
        bodyParams.add(WhatsAppParameter.builder().type("text").text(String.valueOf(daysRemaining)).build());

        List<WhatsAppComponent> components = new ArrayList<>();
        components.add(WhatsAppComponent.builder()
                .type("body")
                .parameters(bodyParams)
                .build());

        sendTemplateMessage(phoneNumber, subscriptionExpiringSoonTemplate, "fr", components);
    }
}
