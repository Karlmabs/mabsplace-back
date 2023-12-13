package com.mabsplace.mabsplaceback.domain.dtos.payment;

import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.myService.MyServiceResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserResponseDto;
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

  private UserResponseDto user;

  private BigDecimal amount;
  private Date paymentDate;

  private CurrencyResponseDto currency;

  private MyServiceResponseDto service;

  private SubscriptionPlanResponseDto subscriptionPlan;

  private PaymentStatus status;

}
