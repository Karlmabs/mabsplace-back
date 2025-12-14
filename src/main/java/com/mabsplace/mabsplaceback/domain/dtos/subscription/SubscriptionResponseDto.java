package com.mabsplace.mabsplaceback.domain.dtos.subscription;

import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    private Long profileId; // Nullable - allow null when no profile assigned

    private Long accountId; // Nullable - allow null when no account assigned

    private String profilePin;

    private String login;

    private String password;

    private String profileName;

    private long serviceId;

    private String serviceName;

    private String serviceLogo;

    private Date startDate;

    private Date endDate;

    private long subscriptionPlanId;

    private String subscriptionPlanName;

    private SubscriptionStatus status;

    private boolean autoRenew = true;

    private Integer renewalAttempts = 0;

    private Date lastRenewalAttempt;

    private long nextSubscriptionPlanId;

    private boolean isTrial = false;

    private String accessInstructions;

    // Service credential visibility configuration
    private Boolean showAccountCredentials;

    private Boolean showProfileName;

    private Boolean showProfilePin;
}
