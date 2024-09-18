package com.mabsplace.mabsplaceback.domain.dtos.coolpay;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CallbackResquest {
    private String application;
    private String appTransactionRef;
    private String operatorTransactionRef;
    private String transactionRef;
    private String transactionType;
    private int transactionAmount;
    private int transactionFees;
    private String transactionCurrency;
    private String transactionOperator;
    private String transactionStatus;
    private String transactionReason;
    private String transactionMessage;
    private String customerPhoneNumber;
    private String signature;
}
