package com.mabsplace.mabsplaceback.domain.dtos.coolpay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PayoutRequest {
    private Double transaction_amount;
    private String transaction_currency;
    private String transaction_reason;
    private String transaction_operator;
    private String app_transaction_ref;
    private String customer_phone_number;
    private String customer_name;
    private String customer_email;
    private String customer_lang;
}
