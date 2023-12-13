package com.mabsplace.mabsplaceback.domain.dtos.wallet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WalletRequestDto implements Serializable {

  private long userId;

  private long currencyId;
}
