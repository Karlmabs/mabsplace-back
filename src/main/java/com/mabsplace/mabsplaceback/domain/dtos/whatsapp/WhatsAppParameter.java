package com.mabsplace.mabsplaceback.domain.dtos.whatsapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppParameter {
    private String type;  // "text", "currency", "date_time", etc.
    private String text;  // For type "text"
}
