package com.mabsplace.mabsplaceback.domain.dtos.onesignal;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class OneSignalSMSRequest {
    private String phoneNumber;
    private String message;
    private String templateId;
    private Map<String, String> customData;
}
