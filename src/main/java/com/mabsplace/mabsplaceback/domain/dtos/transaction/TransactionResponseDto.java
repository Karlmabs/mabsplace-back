package com.mabsplace.mabsplaceback.domain.dtos.transaction;

import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.wallet.WalletResponseDto;
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
public class TransactionResponseDto implements Serializable {
  private Long id;
  private WalletResponseDto senderWallet;

  private WalletResponseDto receiverWallet;

  private BigDecimal amount;
  private Date transactionDate;
  private CurrencyResponseDto currency;
}
