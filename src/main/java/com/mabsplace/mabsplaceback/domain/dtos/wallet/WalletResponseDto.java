package com.mabsplace.mabsplaceback.domain.dtos.wallet;

import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserResponseDto;
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
  private UserResponseDto user;
  private BigDecimal balance;
  private CurrencyResponseDto currency;
}
