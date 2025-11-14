package com.mabsplace.mabsplaceback.domain.dtos.withdrawal;

import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserResponseDto;
import com.mabsplace.mabsplaceback.domain.enums.WithdrawalOperator;
import com.mabsplace.mabsplaceback.domain.enums.WithdrawalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WithdrawalResponseDto implements Serializable {
  private Long id;
  private BigDecimal amount;
  private CurrencyResponseDto currency;
  private WithdrawalOperator transactionOperator;
  private String customerName;
  private String customerPhoneNumber;
  private String customerEmail;
  private String customerLang;
  private String customerUsername;
  private String transactionReason;
  private String appTransactionRef;
  private String coolpayTransactionRef;
  private WithdrawalStatus status;
  private UserResponseDto createdBy;
  private String errorMessage;
  private Date createdAt;
  private Date updatedAt;
}
