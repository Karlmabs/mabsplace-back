package com.mabsplace.mabsplaceback.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Getter
public class OneSignalConfig {

    @Value("${onesignal.app.id}")
    private String appId;

    @Value("${onesignal.api.key}")
    private String apiKey;

    @Value("${onesignal.api.url}")
    private String apiUrl;

    @Value("${onesignal.user.auth.key:}")
    private String userAuthKey;

    @Value("${onesignal.email.from.default}")
    private String defaultFromEmail;

    /**
     * WebClient bean configured for OneSignal API calls
     */
    @Bean(name = "oneSignalWebClient")
    public WebClient oneSignalWebClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Key " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
