package com.mabsplace.mabsplaceback.domain.dtos.onesignal;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class OneSignalEmailRequest {
    private String to;
    private String subject;
    private String templateId;
    private Map<String, String> customData;
    private String fromEmail;
}
