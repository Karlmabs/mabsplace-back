package com.mabsplace.mabsplaceback.domain.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PasswordResetResponseDto implements Serializable {
    private String generatedPassword;
    private String message;
    private Long userId;
    private String username;
}
