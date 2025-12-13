package com.mabsplace.mabsplaceback.domain.dtos.onesignal;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class OneSignalPushRequest {
    private String externalUserId; // User.id
    private String title;
    private String message;
    private String templateId;
    private Map<String, Object> data;
    private String url; // Deep link
}
