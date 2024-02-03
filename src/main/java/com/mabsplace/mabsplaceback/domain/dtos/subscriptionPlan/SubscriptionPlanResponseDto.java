package com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan;

import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyResponseDto;
import com.mabsplace.mabsplaceback.domain.enums.Period;
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
public class SubscriptionPlanResponseDto implements Serializable {
  private Long id;

  private String name; // e.g., "Monthly", "Yearly"

  private BigDecimal price;

  private Period period;

  private String description;

  private long myServiceId;

  private CurrencyResponseDto currency;
}
