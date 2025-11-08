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
public class WhatsAppComponent {
    private String type;  // "header", "body", "button"
    private List<WhatsAppParameter> parameters;
    private String subType;  // For buttons: "url", "quick_reply"
    private Integer index;  // For buttons
}
