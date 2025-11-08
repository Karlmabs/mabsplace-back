package com.mabsplace.mabsplaceback.domain.dtos.whatsapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppResponse {
    private String messagingProduct;
    private WhatsAppContact[] contacts;
    private WhatsAppMessage[] messages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WhatsAppContact {
        private String input;
        private String waId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WhatsAppMessage {
        private String id;
    }
}
