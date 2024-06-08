package com.mabsplace.mabsplaceback.security.request;


import lombok.Data;

@Data
public class VerifyCodeRequest {
    private String email;
    private String code;
}