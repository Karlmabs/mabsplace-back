package com.mabsplace.mabsplaceback.domain.dtos.notification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PushNotificationRequest {
    private List<Long> userIds;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    private Map<String, Object> data = new HashMap<>();
}
