package com.mabsplace.mabsplaceback.security.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";

    private Object user;

    public AuthResponse(String accessToken, Object user) {
        this.accessToken = accessToken;
        this.user = user;
    }

}
