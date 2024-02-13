package com.mabsplace.mabsplaceback.domain.dtos.payment;

import com.mabsplace.mabsplaceback.domain.enums.PaymentStatus;
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
public class PaymentResponseDto {
  private Long id;

  private long userId;

  private BigDecimal amount;
  private Date paymentDate;

  private long currencyId;
  private String currencyName;
  private long serviceId;
  private String serviceName;
  private long subscriptionPlanId;
  private String subscriptionPlanName;

  private PaymentStatus status;

}
