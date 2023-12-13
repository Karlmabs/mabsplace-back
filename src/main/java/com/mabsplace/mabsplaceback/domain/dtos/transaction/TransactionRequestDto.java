package com.mabsplace.mabsplaceback.domain.dtos.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionRequestDto implements Serializable {
  private long senderWalletId;

  private long receiverWalletId;

  private Double amount;
  private Date transactionDate;
  private long currencyId;
}
