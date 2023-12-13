package com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan;

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
public class SubscriptionPlanRequestDto implements Serializable {

  private String name; // e.g., "Monthly", "Yearly"

  private BigDecimal price;

  private String description;

  private long myServiceId;

  private long currencyId;
}
