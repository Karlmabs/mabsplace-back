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
public class SubscriptionLightweightResponseDto implements Serializable {

    private Long id;
    private Long userId;
    private String username;
    private String serviceName;
    private String subscriptionPlanName;
    private Date startDate;
    private Date endDate;
    private SubscriptionStatus status;
    private boolean autoRenew;
    private boolean isTrial;
}
