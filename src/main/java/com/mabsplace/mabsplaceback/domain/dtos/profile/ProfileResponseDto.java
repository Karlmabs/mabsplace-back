package com.mabsplace.mabsplaceback.domain.dtos.profile;

import com.mabsplace.mabsplaceback.domain.dtos.serviceAccount.ServiceAccountResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProfileResponseDto implements Serializable {
  private Long id;
  private ServiceAccountResponseDto serviceAccount;
  private SubscriptionResponseDto subscription;
  private String profileName;
}
