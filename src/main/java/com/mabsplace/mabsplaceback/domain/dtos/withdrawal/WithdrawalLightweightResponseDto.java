package com.mabsplace.mabsplaceback.domain.dtos.withdrawal;

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
public class WithdrawalLightweightResponseDto implements Serializable {
  private Long id;
  private BigDecimal amount;
  private String currencySymbol;
  private String currencyName;
  private WithdrawalOperator transactionOperator;
  private String customerName;
  private String customerPhoneNumber;
  private String appTransactionRef;
  private String coolpayTransactionRef;
  private WithdrawalStatus status;
  private String createdByUsername;
  private Date createdAt;
}
