package com.mabsplace.mabsplaceback.domain.dtos.subscription;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SubscriptionRequestDto implements Serializable {

  private long userId;

  private long subscriptionPlanId;

  private long serviceId;

  private Date startDate;
}
