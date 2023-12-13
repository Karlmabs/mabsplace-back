package com.mabsplace.mabsplaceback.domain.dtos.subscription;

import com.mabsplace.mabsplaceback.domain.dtos.profile.ProfileResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SubscriptionResponseDto implements Serializable {
  private Long id;
  private UserResponseDto user;

  private SubscriptionPlanResponseDto subscriptionPlan;

  private ProfileResponseDto profile;

  private Date startDate;

  private Date endDate;
}
