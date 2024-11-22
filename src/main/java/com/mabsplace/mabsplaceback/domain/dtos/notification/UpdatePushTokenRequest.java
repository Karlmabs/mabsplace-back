package com.mabsplace.mabsplaceback.domain.dtos.notification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePushTokenRequest {
    @NotBlank(message = "Push token is required")
    private String pushToken;
}