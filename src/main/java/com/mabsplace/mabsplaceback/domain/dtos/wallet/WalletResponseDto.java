package com.mabsplace.mabsplaceback.domain.dtos.wallet;

import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyResponseDto;
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
public class WalletResponseDto implements Serializable {
  private long userId;
  private BigDecimal balance;
  private CurrencyResponseDto currency;
}
