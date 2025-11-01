package com.mabsplace.mabsplaceback.domain.dtos.subscription;

import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
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

    private long profileId;

    private SubscriptionStatus status;

    private boolean autoRenew = true;

    private Integer renewalAttempts = 0;

    private Date lastRenewalAttempt;

    private long nextSubscriptionPlanId;

    private Boolean isTrial = false;

    private String accessInstructions;

}
