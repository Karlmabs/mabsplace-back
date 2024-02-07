package com.mabsplace.mabsplaceback.domain.dtos.subscription;

import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
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

  private long userId;

  private String username;

  private long profileId;

  private String profileName;

  private long serviceId;

  private String serviceName;

  private Date startDate;

  private Date endDate;

  private long subscriptionPlanId;

  private String subscriptionPlanName;

  private SubscriptionStatus status;
}
