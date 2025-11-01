package com.mabsplace.mabsplaceback.domain.dtos.myService;

import com.mabsplace.mabsplaceback.domain.dtos.serviceAccount.ServiceAccountResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MyServiceResponseDto implements Serializable {
  private Long id;
  private String name;
  private String logo;
  private String image;
  private String description;
  private Boolean showAccountCredentials;
  private Boolean showProfileName;
  private Boolean showProfilePin;
  private List<ServiceAccountResponseDto> serviceAccounts;
  private Set<SubscriptionPlanResponseDto> subscriptionPlans;
}
