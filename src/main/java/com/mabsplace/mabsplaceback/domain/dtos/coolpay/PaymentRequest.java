package com.mabsplace.mabsplaceback.domain.dtos.coolpay;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaymentRequest {
    private Double transaction_amount;
    private String transaction_currency;
    private String transaction_reason;
    private String app_transaction_ref;
    private String customer_phone_number;
    private String customer_name;
    private String customer_email;
    private String customer_lang;
}
