package com.mabsplace.mabsplaceback.domain.dtos.whatsapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppTemplateRequest {
    private String name;  // Template name
    private WhatsAppLanguage language;
    private List<WhatsAppComponent> components;
}
