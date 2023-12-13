package com.mabsplace.mabsplaceback.domain.dtos.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentRequestDto {
  private long userId;

  private BigDecimal amount;
  private Date paymentDate;
  private long currencyId;
  private long serviceId;
  private long subscriptionPlanId;
}
