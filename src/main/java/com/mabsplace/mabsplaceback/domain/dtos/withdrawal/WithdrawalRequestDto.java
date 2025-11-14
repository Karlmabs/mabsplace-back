package com.mabsplace.mabsplaceback.domain.dtos.withdrawal;

import com.mabsplace.mabsplaceback.domain.enums.WithdrawalOperator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WithdrawalRequestDto implements Serializable {
  private BigDecimal transactionAmount;
  private String transactionCurrency;
  private String transactionReason;
  private WithdrawalOperator transactionOperator;
  private String appTransactionRef;
  private String customerPhoneNumber;
  private String customerName;
  private String customerEmail;
  private String customerLang; // en or fr
  private String customerUsername; // For MCP withdrawals
  private Long currencyId;
  private Long createdByUserId;
}
