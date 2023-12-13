package com.mabsplace.mabsplaceback.domain.dtos.transaction;

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
public class TransactionRequestDto implements Serializable {
  private long senderWalletId;

  private long receiverWalletId;

  private BigDecimal amount;
  private Date transactionDate;
  private long currencyId;
}
