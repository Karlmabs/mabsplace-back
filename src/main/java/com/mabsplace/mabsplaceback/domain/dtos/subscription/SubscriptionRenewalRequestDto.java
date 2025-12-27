package com.mabsplace.mabsplaceback.domain.dtos.subscription;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SubscriptionRenewalRequestDto {
    private Long subscriptionId;
    private Long newPlanId;      // Optional - use current plan if null
    private String promoCode;    // Optional promo code
}
