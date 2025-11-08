package com.mabsplace.mabsplaceback.domain.dtos.whatsapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppLanguage {
    private String code;  // e.g., "en", "fr", "en_US"
}
