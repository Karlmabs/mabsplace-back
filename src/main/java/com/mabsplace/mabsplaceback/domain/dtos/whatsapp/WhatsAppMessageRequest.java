package com.mabsplace.mabsplaceback.domain.dtos.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppMessageRequest {
    @JsonProperty("messaging_product")
    private String messagingProduct;  // Always "whatsapp"

    private String to;  // Phone number with country code
    private String type;  // "template" for template messages
    private WhatsAppTemplateRequest template;
}
