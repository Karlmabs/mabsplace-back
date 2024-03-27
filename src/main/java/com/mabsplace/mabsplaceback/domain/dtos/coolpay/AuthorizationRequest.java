package com.mabsplace.mabsplaceback.domain.dtos.coolpay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthorizationRequest {
    private String transactionRef;
    private String code;
}
