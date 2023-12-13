package com.mabsplace.mabsplaceback.domain.dtos.currency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CurrencyResponseDto {
  private Long id;
  private String name;
  private Double exchangeRate;
}
